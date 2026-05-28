package com.example.jwt_demo.repository;

import com.example.jwt_demo.model.Landmark;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LandmarkRepository extends MongoRepository<Landmark, String> {
}
