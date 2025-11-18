package com.example.jwt_demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatMessage {

    private String conversationId;
    private String userId;   // اللي بعت الرسالة
    private String sender;   // "user" أو "bot"
    private String content;
    private LocalDateTime createdAt;
}
