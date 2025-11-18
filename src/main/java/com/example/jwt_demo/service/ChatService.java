package com.example.jwt_demo.service;

import com.example.jwt_demo.model.ChatConversation;
import com.example.jwt_demo.model.ChatMessage;
import com.example.jwt_demo.repository.ConversationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatService {

    private final ConversationRepository chatRepository;
    private final AIService aiService;

    public ChatService(ConversationRepository chatRepository, AIService aiService) {
        this.chatRepository = chatRepository;
        this.aiService = aiService;
    }

    public ChatConversation sendMessage(String userId, String message, String sender, String conversationId) {

        ChatConversation conversation;

        if (conversationId == null || conversationId.isEmpty()) {
            // إنشاء محادثة جديدة
            conversation = new ChatConversation();
            conversation.setUserId(userId);
            conversation.setConversationId(UUID.randomUUID().toString());
            conversation.setMessages(new ArrayList<>());
            conversation.setCreatedAt(LocalDateTime.now());
        } else {
            // استخدام محادثة موجودة
            conversation = chatRepository
                    .findByConversationIdAndUserId(conversationId, userId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        }

        // رسالة المستخدم
        ChatMessage userMsg = new ChatMessage(
                conversation.getConversationId(),
                userId,
                sender,
                message,
                LocalDateTime.now()
        );
        conversation.getMessages().add(userMsg);

        // الرد من البوت
        if (!sender.equals("bot")) {
            String aiReply = aiService.getReply(message);
            ChatMessage botMsg = new ChatMessage(
                    conversation.getConversationId(),
                    "bot",
                    "bot",
                    aiReply,
                    LocalDateTime.now()
            );
            conversation.getMessages().add(botMsg);
        }

        conversation.setUpdatedAt(LocalDateTime.now());
        return chatRepository.save(conversation);
    }

    // جلب كل المحادثات الخاصة بمستخدم
    public List<ChatConversation> getUserConversations(String userId) {
        return chatRepository.findByUserId(userId);
    }

    // جلب محادثة محددة
    public Optional<ChatConversation> getConversation(String conversationId, String userId) {
        return chatRepository.findByConversationIdAndUserId(conversationId, userId);
    }
}
