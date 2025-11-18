package com.example.jwt_demo.model;

import lombok.Data;

@Data
public class ChatRequest {
    private String userId;
    private String sender;  // "user"
    private String message;
    private String conversationId; // يمكن null أو "" لفتح محادثة جديدة
}
