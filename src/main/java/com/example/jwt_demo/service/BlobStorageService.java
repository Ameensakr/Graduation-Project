package com.example.jwt_demo.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class BlobStorageService {

    private static final Logger log = LoggerFactory.getLogger(BlobStorageService.class);

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    @Value("${azure.storage.connection.string:}")
    private String connectionString;

    @Value("${azure.storage.profile.container}")
    private String profileContainer;

    @Value("${azure.storage.public.container}")
    private String publicContainer;

    private BlobServiceClient client;

    @PostConstruct
    void init() {
        if (connectionString == null || connectionString.isBlank()) {
            log.warn("Azure Storage connection string not configured. Image uploads will fail.");
            return;
        }
        this.client = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    public String uploadProfile(MultipartFile file) {
        return upload(file, profileContainer);
    }

    public String uploadPublic(MultipartFile file) {
        return upload(file, publicContainer);
    }

    private String upload(MultipartFile file, String container) {
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Storage is not configured");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File exceeds 5MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed (jpeg, png, webp, gif)");
        }

        BlobContainerClient containerClient = client.getBlobContainerClient(container);
        String blobName = UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());
        BlobClient blob = containerClient.getBlobClient(blobName);

        try {
            blob.upload(file.getInputStream(), file.getSize(), true);
            blob.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file");
        }

        return blob.getBlobUrl();
    }

    public void deleteByUrl(String url) {
        if (client == null || url == null || url.isBlank()) return;
        try {
            String accountUrl = client.getAccountUrl();
            if (!url.startsWith(accountUrl)) {
                log.debug("Skipping delete; URL is not in our storage account: {}", url);
                return;
            }
            String relative = url.substring(accountUrl.length() + 1);
            int slash = relative.indexOf('/');
            if (slash < 0) return;
            String container = relative.substring(0, slash);
            String blobName = relative.substring(slash + 1);
            client.getBlobContainerClient(container).getBlobClient(blobName).deleteIfExists();
        } catch (Exception e) {
            log.warn("Failed to delete blob {}: {}", url, e.getMessage());
        }
    }

    private static String sanitize(String name) {
        if (name == null) return "file";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
