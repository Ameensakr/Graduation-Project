package com.example.jwt_demo.repository;

import com.example.jwt_demo.model.SuggestedPlan;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SuggestedPlanRepository extends MongoRepository<SuggestedPlan, String> {
}
