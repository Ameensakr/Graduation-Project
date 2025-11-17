package com.example.jwt_demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "users") // اسم الـ collection في MongoDB
public class User {

    @Id
    private String id;

    private String username;
    private String email;
    private String password;

    // الحقول الخاصة بالـ refresh token
    private String refreshTokenHash;
    private Date refreshTokenExpiry;

    // ======= Constructors =======
    public User() {
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // ======= Getters & Setters =======
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public void setRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }

    public Date getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }

    public void setRefreshTokenExpiry(Date refreshTokenExpiry) {
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    // ======= Helper =======
    public boolean isRefreshTokenValid(String token, org.springframework.security.crypto.password.PasswordEncoder encoder) {
        if (refreshTokenHash == null || refreshTokenExpiry == null) return false;
        if (refreshTokenExpiry.before(new Date())) return false;
        return encoder.matches(token, refreshTokenHash);
    }
}
