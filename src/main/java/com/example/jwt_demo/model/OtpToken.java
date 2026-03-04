package com.example.jwt_demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "otp_tokens")
public class OtpToken {
    @Id
    private String id;
    private String email;
    private String otp;
    private LocalDateTime expirationTime;

    public OtpToken(String email, String otp, LocalDateTime expirationTime) {
        this.email = email;
        this.otp = otp;
        this.expirationTime = expirationTime;
    }
}
