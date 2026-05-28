package com.example.jwt_demo.service;

import com.example.jwt_demo.dto.AIReply;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatService {

    private static final int HISTORY_LIMIT = 20;

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
            List<MultipartFile> files,
            String type) {

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

        List<Map<String, String>> history = buildHistory(conversation.getMessages());

        // Message
        ChatMessage userMessage = new ChatMessage(
                conversation.getConversationId(),
                userId,
                "user",
                message,
                "chat",
                null,
                LocalDateTime.now()
        );

        conversation.getMessages().add(userMessage);
        conversation.setUpdatedAt(LocalDateTime.now());

        try {
            // Bot reply
            AIReply reply = aiService.getReply(message, files, type, history);

            ChatMessage botMessage = new ChatMessage(
                    conversation.getConversationId(),
                    null,
                    "bot",
                    reply.getContent(),
                    reply.getType(),
                    reply.getData(),
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

        List<Map<String, String>> history = buildHistory(messages.subList(0, messages.size() - 1));

        AIReply reply = aiService.getReply(last.getContent(), null, last.getType(), history);
        ChatMessage botMessage = new ChatMessage(
                conversation.getConversationId(),
                null,
                "bot",
                reply.getContent(),
                reply.getType(),
                reply.getData(),
                LocalDateTime.now()
        );
        conversation.getMessages().add(botMessage);
        conversation.setUpdatedAt(LocalDateTime.now());
        return conversationRepository.save(conversation);
    }

    private List<Map<String, String>> buildHistory(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) return List.of();
        int from = Math.max(0, messages.size() - HISTORY_LIMIT);
        List<Map<String, String>> history = new ArrayList<>();
        for (ChatMessage m : messages.subList(from, messages.size())) {
            String role = "user".equals(m.getSender()) ? "user" : "Soli";
            String content = m.getContent() != null ? m.getContent() : "";
            Map<String, String> turn = new HashMap<>();
            turn.put("role", role);
            turn.put("content", content);
            history.add(turn);
        }
        return history;
    }

    public void deleteConversation(String conversationId, String userId) {
        ChatConversation conv = conversationRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        conversationRepository.delete(conv);
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
