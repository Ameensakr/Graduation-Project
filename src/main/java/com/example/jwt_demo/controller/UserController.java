package com.example.jwt_demo.controller;

import com.example.jwt_demo.model.User;
import com.example.jwt_demo.service.UserService;
import com.example.jwt_demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // ---------------------- REGISTER ----------------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        if (username == null || email == null || password == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));

        boolean ok = userService.registerUser(username, email, password);

        if (!ok)
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    // ---------------------- LOGIN ----------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Missing email or password"));

        if (!userService.loginUserByEmail(email, password))
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));

        String accessToken = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        userService.storeRefreshTokenForUser(email, refreshToken);

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // لو HTTPS استخدم true
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "message", "Login successful"
        ));
    }

    // ---------------------- REFRESH TOKEN ----------------------
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refreshToken".equals(c.getName())) refreshToken = c.getValue();
            }
        }

        if (refreshToken == null)
            return ResponseEntity.status(401).body(Map.of("error", "Missing refresh token"));

        String email;
        try {
            email = jwtUtil.extractUsername(refreshToken);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
        }

        if (!userService.validateRefreshToken(email, refreshToken))
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired refresh token"));

        String newAccessToken = jwtUtil.generateAccessToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);
        userService.storeRefreshTokenForUser(email, newRefreshToken);

        Cookie cookie = new Cookie("refreshToken", newRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "message", "Token refreshed successfully"
        ));
    }

    // ---------------------- LOGOUT محسّن ----------------------
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refreshToken".equals(c.getName())) {
                    try {
                        String email = jwtUtil.extractUsername(c.getValue());
                        userService.revokeRefreshToken(email);
                    } catch (Exception ignored) {}
                    // مسح الكوكيز
                    c.setValue("");
                    c.setPath("/");
                    c.setMaxAge(0);
                    c.setHttpOnly(true);
                    c.setSecure(false);
                    response.addCookie(c);
                }
            }
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ---------------------- PROFILE محسّن ----------------------
    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestHeader(value="Authorization", required=false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid Authorization header"));

        String token = authHeader.substring(7);
        String email;
        try {
            email = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        if (jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Token expired"));
        }

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty())
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));

        User user = userOpt.get();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "message", "Welcome " + user.getUsername()
        ));
    }
}
