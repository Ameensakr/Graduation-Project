package com.example.jwt_demo.repository;

import com.example.jwt_demo.model.ChatConversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<ChatConversation, String> {

    // One chat
    Optional<ChatConversation> findByConversationIdAndUserId(String conversationId, String userId);

    // All chats for user
    List<ChatConversation> findByUserId(String userId);
}
