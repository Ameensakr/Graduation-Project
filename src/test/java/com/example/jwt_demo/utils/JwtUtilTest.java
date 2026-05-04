package com.example.jwt_demo.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "this-is-a-very-long-test-secret-key-for-hs256-please";
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenValidityMillis", 60_000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenValidityMillis", 600_000L);
    }

    @Test
    void generateAccessToken_thenExtractUsername_returnsEmail() {
        String token = jwtUtil.generateAccessToken("a@b.com");

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("a@b.com");
    }

    @Test
    void generateRefreshToken_thenExtractUsername_returnsEmail() {
        String token = jwtUtil.generateRefreshToken("a@b.com");
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("a@b.com");
    }

    @Test
    void validateToken_correctEmail_returnsTrue() {
        String token = jwtUtil.generateAccessToken("a@b.com");
        assertThat(jwtUtil.validateToken(token, "a@b.com")).isTrue();
    }

    @Test
    void validateToken_wrongEmail_returnsFalse() {
        String token = jwtUtil.generateAccessToken("a@b.com");
        assertThat(jwtUtil.validateToken(token, "x@y.com")).isFalse();
    }

    @Test
    void validateToken_garbageToken_returnsFalse() {
        assertThat(jwtUtil.validateToken("not-a-jwt", "a@b.com")).isFalse();
    }

    @Test
    void isTokenExpired_freshToken_false() {
        String token = jwtUtil.generateAccessToken("a@b.com");
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    void isTokenExpired_pastExpiry_true() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expired = Jwts.builder()
                .setSubject("a@b.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10_000))
                .setExpiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(key)
                .compact();

        assertThat(jwtUtil.isTokenExpired(expired)).isTrue();
        assertThat(jwtUtil.validateToken(expired, "a@b.com")).isFalse();
    }

    @Test
    void extractExpiration_isAfterNow() {
        String token = jwtUtil.generateAccessToken("a@b.com");
        assertThat(jwtUtil.extractExpiration(token)).isAfter(new Date());
    }
}
