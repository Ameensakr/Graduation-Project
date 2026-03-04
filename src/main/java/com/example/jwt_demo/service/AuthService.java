package com.example.jwt_demo.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.jwt_demo.dto.ResetPasswordRequest;
import com.example.jwt_demo.model.OtpToken;
import com.example.jwt_demo.model.User;
import com.example.jwt_demo.repository.OtpTokenRepository;
import com.example.jwt_demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public void requestPasswordReset(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (userRepository.findByEmail(email).isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpTokenRepository.save(new OtpToken(email, otp, LocalDateTime.now().plusMinutes(10)));
        emailService.sendOtpEmail(email, otp);
    }

    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        String newPassword = request.getNewPassword();

        if (email == null || otp == null || newPassword == null) {
            throw new IllegalArgumentException("Missing required fields");
        }

        OtpToken token = otpTokenRepository.findByEmailAndOtp(email, otp)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP"));

        if (token.getExpirationTime().isBefore(LocalDateTime.now())) {
            otpTokenRepository.delete(token);
            throw new IllegalArgumentException("OTP Expired");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpTokenRepository.delete(token);
    }
}
