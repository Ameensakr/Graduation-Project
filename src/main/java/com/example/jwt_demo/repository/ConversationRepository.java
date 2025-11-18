package com.example.jwt_demo.repository;

import com.example.jwt_demo.model.ChatConversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<ChatConversation, String> {

    // تجيب المحادثة بناءً على conversationId و userId
    Optional<ChatConversation> findByConversationIdAndUserId(String conversationId, String userId);

    // تجيب كل المحادثات الخاصة بالمستخدم
    List<ChatConversation> findByUserId(String userId);
}
