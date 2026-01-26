package com.example.jwt_demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document("chat_conversations")
public class ChatConversation {

    @Id
    private String id;   // _id في MongoDB

    private String conversationId;

    private String userId;
    private List<ChatMessage> messages;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
