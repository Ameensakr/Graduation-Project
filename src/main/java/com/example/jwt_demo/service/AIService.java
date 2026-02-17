package com.example.jwt_demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    @Value("${ai.model.url:}")
    private String aiModelUrl;

    private final RestTemplate restTemplate;

    public AIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Call your friend's model API
     * Friend's API format:
     * - Endpoint: /ask
     * - Request body: {"question": "user message"}
     * - Response: {"answer": "AI response"}
     */
    public String getReply(String userMessage) {
        // If AI model URL is not configured, return error
        if (aiModelUrl == null || aiModelUrl.isEmpty() || aiModelUrl.contains("your-friend-model-url")) {
            log.error("AI model URL not configured");
            return "Error: AI model URL is not configured. Please check application.properties";
        }

        try {
            // Call friend's /ask endpoint
            Map<String, Object> requestBody = Map.of(
                "question", userMessage
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("Calling AI model at: {}/ask", aiModelUrl);

            ResponseEntity<Map> response = restTemplate.exchange(
                aiModelUrl + "/ask",
                HttpMethod.POST,
                request,
                Map.class
            );

            // Parse response
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("answer")) {
                return (String) body.get("answer");
            }

            // Check for error in response
            if (body != null && body.containsKey("error")) {
                log.error("AI model returned error: {}", body.get("error"));
                return "Sorry, the AI service encountered an error.";
            }

            log.warn("Unexpected response format from AI model: {}", body);
            return "Sorry, I couldn't process your request.";

        } catch (Exception e) {
            log.error("Error calling AI model: {}", e.getMessage(), e);
            return "Sorry, there was an error connecting to the AI service. Please try again.";
        }
    }
}
