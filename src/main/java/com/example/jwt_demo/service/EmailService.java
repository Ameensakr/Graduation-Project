package com.example.jwt_demo.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tripplanner143@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Your Password Reset OTP");
        message.setText("Your OTP for password reset is: " + otp + "\nIt is valid for 10 minutes.");

        javaMailSender.send(message);
    }
}
