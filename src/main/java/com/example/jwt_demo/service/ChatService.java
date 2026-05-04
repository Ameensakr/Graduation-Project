package com.example.jwt_demo.service;

import com.example.jwt_demo.exception.AIServiceException;
import com.example.jwt_demo.model.ChatConversation;
import com.example.jwt_demo.model.ChatMessage;
import com.example.jwt_demo.repository.ConversationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
            List<MultipartFile> files) {

        ChatConversation conversation;

        // New chat
        if (conversationId == null || conversationId.isBlank()) {

            conversation = new ChatConversation();
            conversation.setConversationId(UUID.randomUUID().toString());
            conversation.setUserId(userId);
            conversation.setMessages(new ArrayList<>());
            conversation.setCreatedAt(LocalDateTime.now());
            conversation.setUpdatedAt(LocalDateTime.now());
        }
        // Old chat
        else {
            conversation = conversationRepository
                    .findByConversationIdAndUserId(conversationId, userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
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
        conversation.setUpdatedAt(LocalDateTime.now());

        try {
            // Bot reply
            String aiReply = aiService.getReply(message, files);

            ChatMessage botMessage = new ChatMessage(
                    conversation.getConversationId(),
                    null,
                    "bot",
                    aiReply,
                    LocalDateTime.now()
            );

            conversation.getMessages().add(botMessage);
            conversation.setUpdatedAt(LocalDateTime.now());

            if(conversationId == null || conversationId.isBlank()) {
                String title = aiService.generateTitle(message);
                if (title == null || title.isEmpty()) {
                    title = message.length() > 30 ? message.substring(0, 30) + "..." : message;
                }
                conversation.setTitle(title);
            }

            return conversationRepository.save(conversation);
        } catch (AIServiceException e) {
            conversationRepository.save(conversation);
            throw e;
        }

    }

    public ChatConversation regenerateLastReply(String conversationId, String userId) {
        ChatConversation conversation = conversationRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        List<ChatMessage> messages = conversation.getMessages();
        if (messages.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation has no messages");
        }

        ChatMessage last = messages.get(messages.size() - 1);
        if(!"user".equals(last.getSender())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Last message is not from user; nothing to regenerate");
        }
        String aiReply = aiService.getReply(last.getContent(), null);
        ChatMessage botMessage = new ChatMessage(conversation.getConversationId(), null, "bot", aiReply, LocalDateTime.now());
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
