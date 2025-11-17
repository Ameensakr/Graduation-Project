package com.example.jwt_demo.service;

import com.example.jwt_demo.model.ChatConversation;
import com.example.jwt_demo.model.ChatMessage;
import com.example.jwt_demo.repository.ConversationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ConversationRepository chatRepository;
    private final AIService aiService;

    public ChatService(ConversationRepository chatRepository, AIService aiService) {
        this.chatRepository = chatRepository;
        this.aiService = aiService;
    }

    // إرسال رسالة + إضافة رد AI
    public ChatConversation sendMessage(String userId, String message, String sender) {
        List<ChatConversation> userConversations = chatRepository.findByUserId(userId);

        ChatConversation conversation;
        if (userConversations.isEmpty()) {
            conversation = new ChatConversation();
            conversation.setUserId(userId);
            conversation.setMessages(new java.util.ArrayList<>());
            conversation.setCreatedAt(LocalDateTime.now());
        } else {
            conversation = userConversations.get(userConversations.size() - 1);
        }

        // رسالة المستخدم
        ChatMessage userMsg = new ChatMessage(sender, message, LocalDateTime.now());
        conversation.getMessages().add(userMsg);

        // رسالة AI (dummy)
        if (!sender.equals("bot")) {
            String aiReply = aiService.getReply(message);
            ChatMessage botMsg = new ChatMessage("bot", aiReply, LocalDateTime.now());
            conversation.getMessages().add(botMsg);
        }

        conversation.setUpdatedAt(LocalDateTime.now());
        return chatRepository.save(conversation);
    }

    public List<ChatConversation> getUserConversations(String userId) {
        return chatRepository.findByUserId(userId);
    }

    public Optional<ChatConversation> getConversation(String conversationId, String userId) {
        return chatRepository.findByIdAndUserId(conversationId, userId);
    }
}
