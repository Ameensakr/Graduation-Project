package com.example.jwt_demo.service;

import com.example.jwt_demo.model.ChatConversation;
import com.example.jwt_demo.model.ChatMessage;
import com.example.jwt_demo.repository.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final AIService aiService;

    public ChatService(ConversationRepository conversationRepository,
                       AIService aiService) {
        this.conversationRepository = conversationRepository;
        this.aiService = aiService;
    }

    public ChatConversation sendMessage(
            String userId,
            String message,
            String conversationId,
            MultipartFile file) {

        ChatConversation conversation;

        // New chat
        if (conversationId == null || conversationId.isBlank()) {

            conversation = new ChatConversation();
            conversation.setConversationId(UUID.randomUUID().toString());
            conversation.setUserId(userId);
            conversation.setMessages(new ArrayList<>());
            conversation.setCreatedAt(LocalDateTime.now());
            conversation.setUpdatedAt(LocalDateTime.now());

            String title = aiService.generateTitle(message);
            if (title == null || title.isEmpty()) {
                title = message.length() > 30 ? message.substring(0, 30) + "..." : message;
            }
            conversation.setTitle(title);

        }
        // Old chat
        else {
            conversation = conversationRepository
                    .findByConversationIdAndUserId(conversationId, userId)
                    .orElseThrow(() ->
                            new RuntimeException("Conversation not found or access denied"));
        }

        // Message
        ChatMessage userMessage = new ChatMessage(
                conversation.getConversationId(),
                userId,
                "user",
                message,
                LocalDateTime.now()
        );

        conversation.getMessages().add(userMessage);

        // Bot reply
        String aiReply = aiService.getReply(message, file);

        ChatMessage botMessage = new ChatMessage(
                conversation.getConversationId(),
                "bot",
                "bot",
                aiReply,
                LocalDateTime.now()
        );

        conversation.getMessages().add(botMessage);

        conversation.setUpdatedAt(LocalDateTime.now());

        return conversationRepository.save(conversation);
    }

    // All chats for user
    public List<ChatConversation> getUserConversations(String userId) {
        return conversationRepository.findByUserId(userId);
    }

    // One chat
    public Optional<ChatConversation> getConversation(
            String conversationId,
            String userId
    ) {
        return conversationRepository.findByConversationIdAndUserId(conversationId, userId);
    }
}
