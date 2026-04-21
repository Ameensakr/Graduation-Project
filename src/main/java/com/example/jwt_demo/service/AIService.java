package com.example.jwt_demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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


    public String generateTitle(String userMessage) {
        String prompt = "Generate a short title (max 5 words) for this conversation based on the first message: " + userMessage;
        try {
            Map<String, Object> requestBody = Map.of("question", prompt);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(aiModelUrl+"/ask", HttpMethod.POST ,request, Map.class);

            Map<String, Object> body = response.getBody();
            if(body != null && body.containsKey("answer")) {
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
    public String getReply(String userMessage, MultipartFile file) {
        // If AI model URL is not configured, return error
        if (aiModelUrl == null || aiModelUrl.isEmpty() || aiModelUrl.contains("your-friend-model-url")) {
            log.error("AI model URL not configured");
            return "Error: AI model URL is not configured. Please check application.properties";
        }

        try {
            // Call friend's /ask endpoint


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("question", userMessage);

            if(file != null && !file.isEmpty()) {
                body.add("file", file.getResource());
            }

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            log.info("Calling AI model at: {}/ask", aiModelUrl);

            ResponseEntity<Map> response = restTemplate.exchange(
                aiModelUrl + "/ask",
                HttpMethod.POST,
                request,
                Map.class
            );

            // Parse response
            Map<String, Object> ResoponseBody = response.getBody();
            if (ResoponseBody != null && ResoponseBody.containsKey("answer")) {
                return (String) ResoponseBody.get("answer");
            }

            // Check for error in response
            if (ResoponseBody != null && ResoponseBody.containsKey("error")) {
                log.error("AI model returned error: {}", ResoponseBody.get("error"));
                return "Sorry, the AI service encountered an error.";
            }

            log.warn("Unexpected response format from AI model: {}", ResoponseBody);
            return "Sorry, I couldn't process your request.";

        } catch (Exception e) {
            log.error("Error calling AI model: {}", e.getMessage(), e);
            return "Sorry, there was an error connecting to the AI service. Please try again.";
        }
    }
}
