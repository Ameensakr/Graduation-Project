package com.example.jwt_demo.model;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String conversationId;
}

