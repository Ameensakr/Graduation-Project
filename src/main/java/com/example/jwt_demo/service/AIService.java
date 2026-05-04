package com.example.jwt_demo.service;

import com.example.jwt_demo.exception.AIServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private static final String ASK_PATH = "/ask";

    @Value("${ai.model.url:}")
    private String aiModelUrl;

    private final RestTemplate restTemplate;

    public AIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public String generateTitle(String userMessage) {
        String prompt = "Generate a short title (max 5 words) for this conversation based on the first message: " + userMessage;
        try {
            MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("question", prompt);

            HttpHeaders headers = new HttpHeaders();

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ParameterizedTypeReference<Map<String, Object>> type = new ParameterizedTypeReference<Map<String, Object>>() {
            };
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(aiModelUrl + ASK_PATH, HttpMethod.POST, request, type);

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("answer")) {
                String title = body.get("answer").toString().trim();
                return title.length() > 50 ? title.substring(0, 47) + "..." : title;
            }

        } catch (Exception e) {
            log.error("Error generating title: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Call your friend's model API
     * Friend's API format:
     * - Endpoint: /ask
     * - Request body: {"question": "user message"}
     * - Response: {"answer": "AI response"}
     */
    public String getReply(String userMessage, List<MultipartFile> files) {
        if (aiModelUrl == null || aiModelUrl.isEmpty()) {
            throw new AIServiceException("AI model URL is not configured");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("question", userMessage);

            if (files != null) {
                for (MultipartFile f : files) {
                    if (f != null && !f.isEmpty()) {
                        body.add("files", f.getResource());
                    }
                }
            }

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            log.info("Calling AI model at: {}{}", aiModelUrl, ASK_PATH);

            ParameterizedTypeReference<Map<String, Object>> type = new ParameterizedTypeReference<Map<String, Object>>() {
            };
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    aiModelUrl + ASK_PATH, HttpMethod.POST, request, type);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.get("answer") != null) {
                String answer = responseBody.get("answer").toString();
                if (!answer.isBlank()) return answer;
            }

            if (responseBody != null && responseBody.containsKey("error")) {
                log.error("AI model returned error: {}", responseBody.get("error"));
                throw new AIServiceException("AI model returned error: " + responseBody.get("error"));
            }

            throw new AIServiceException("Unexpected response format from AI model");

        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling AI model: {}", e.getMessage(), e);
            throw new AIServiceException("Error connecting to AI service", e);
        }
    }

}
