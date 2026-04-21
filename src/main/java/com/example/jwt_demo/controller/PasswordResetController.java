package com.example.jwt_demo.controller;

import com.example.jwt_demo.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;

import com.example.jwt_demo.service.AuthService;
import com.example.jwt_demo.dto.ForgotPasswordRequest;
import com.example.jwt_demo.dto.ResetPasswordRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final AuthService authService;
    private final RateLimitingService rateLimitingService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {

        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        
        Bucket bucket = rateLimitingService.resolveForgetBucket(email);


        if (bucket.tryConsume(1)) {
            authService.requestPasswordReset(email);
            return ResponseEntity.ok("OTP sent to your email.");
        }

        return ResponseEntity.status(429).body("Too many requests for this email. Please try again in 10 minutes.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        Bucket bucket = rateLimitingService.resolveResetBucket(email);
        if (bucket.tryConsume(1)) {
            try{
                authService.resetPassword(request);
                return ResponseEntity.ok("Password reset successful.");
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(400).body(e.getMessage());
            }
        }

        return ResponseEntity.status(429).body("Too many requests for this email. Please try again in 10 minutes.");
    }
}
