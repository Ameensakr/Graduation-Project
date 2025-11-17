package com.example.jwt_demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String sender;  // "user" أو "bot"
    private String message;
    private LocalDateTime createdAt;  // وقت إنشاء الرسالة
}
