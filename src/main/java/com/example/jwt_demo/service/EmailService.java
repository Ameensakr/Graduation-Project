package com.example.jwt_demo.service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${azure.communication.connection.string}")
    private String connectionString;

    @Value("${azure.communication.from.email}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        EmailClient emailClient = new EmailClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        EmailMessage message = new EmailMessage()
                .setSenderAddress(fromEmail)
                .setSubject("Your Password Reset OTP")
                .setBodyPlainText("Your OTP for password reset is: " + otp + "\nIt is valid for 10 minutes.")
                .setToRecipients(toEmail); 

        try {
            emailClient.beginSend(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email via Azure: " + e.getMessage());
        }
    }
}
