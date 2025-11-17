package com.example.jwt_demo.repository;

import com.example.jwt_demo.model.ChatConversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<ChatConversation, String> {
    List<ChatConversation> findByUserId(String userId);
    Optional<ChatConversation> findByIdAndUserId(String id, String userId);
}
