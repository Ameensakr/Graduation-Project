package com.example.jwt_demo.service;

import org.springframework.stereotype.Service;

@Service
public class AIService {

    // This is a dummy reply function, it can be connected to OpenAI API or any other AI
    public String getReply(String userMessage) {
        if (userMessage.contains("Pyramids")) {
            return "The Pyramids are located in Giza and can be visited in the morning!";
        }
        return "Hello! How can I help you explore Egypt?";
    }
}
