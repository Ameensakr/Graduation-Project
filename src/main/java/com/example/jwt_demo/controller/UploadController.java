package com.example.jwt_demo.controller;

import com.example.jwt_demo.service.BlobStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final BlobStorageService storage;

    public UploadController(BlobStorageService storage) {
        this.storage = storage;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> upload(
            @AuthenticationPrincipal String email,
            @RequestParam("file") List<MultipartFile> files,
            @RequestParam(value = "visibility", defaultValue = "public") String visibility) {

        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (files.size() > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only one file per request");
        }
        MultipartFile file = files.get(0);

        String url = switch (visibility.toLowerCase()) {
            case "profile" -> storage.uploadProfile(file);
            case "public" -> storage.uploadPublic(file);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "visibility must be 'profile' or 'public'");
        };

        return ResponseEntity.ok(Map.of("url", url));
    }
}
