package com.example.jwt_demo.controller;

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

    private final AuthService authService; // Or wherever you put the logic

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        // Call service method to check email, generate OTP, save it, and send email
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok("OTP sent to your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        // Call service method to validate OTP and update password
        authService.resetPassword(request);
        return ResponseEntity.ok("Password successfully updated.");
    }
}