package com.example.jwt_demo.repository;

import com.example.jwt_demo.model.OtpToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends MongoRepository<OtpToken, String> {
    Optional<OtpToken> findByEmailAndOtp(String email, String otp);

    void deleteByEmail(String email);
}
