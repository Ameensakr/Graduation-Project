package com.example.jwt_demo.repository;

import com.example.jwt_demo.model.SavedPlan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SavedPlanRepository extends MongoRepository<SavedPlan, String> {
    List<SavedPlan> findByUserId(String userId);
    long countByUserId(String userId);
    List<SavedPlan> findByUserIdAndStartDateNotNull(String userId);
}
