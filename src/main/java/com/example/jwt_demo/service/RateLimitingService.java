package com.example.jwt_demo.service;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {
    private final Map<String, Bucket> forgetPasswordCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> resetPasswordCache = new ConcurrentHashMap<>();

    public Bucket resolveForgetBucket(String email) {
        return forgetPasswordCache.computeIfAbsent(email, k -> newBucket(3, 10));
    }

    public Bucket resolveResetBucket(String email) {
        return resetPasswordCache.computeIfAbsent(email, k -> newBucket(5, 30));
    }

    public Bucket newBucket(int capacity, int minutes) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(minutes))))
                .build();
    }

}
