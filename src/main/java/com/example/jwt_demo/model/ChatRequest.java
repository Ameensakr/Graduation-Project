package com.example.jwt_demo.model;

import lombok.Data;

@Data
public class ChatRequest {
    private String userId;
    private String sender;  // "user" أو "bot"
    private String message;
}
