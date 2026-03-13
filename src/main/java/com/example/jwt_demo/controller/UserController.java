package com.example.jwt_demo.controller;
import com.example.jwt_demo.model.User;
import com.example.jwt_demo.service.UserService;
import com.example.jwt_demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @Autowired
    private AuthenticationManager authenticationManager;

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

        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            if(authentication.isAuthenticated()) {
                String accessToken = jwtUtil.generateAccessToken(email);
                String refreshToken = jwtUtil.generateRefreshToken(email);

                Cookie cookie = new Cookie("refreshToken", refreshToken);
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                cookie.setPath("/");
                cookie.setMaxAge(7 * 24 * 60 * 60);
                response.addCookie(cookie);

                return ResponseEntity.ok(Map.of(
                        "accessToken", accessToken,
                        "message", "Login successful"
                ));
            }
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        return ResponseEntity.status(401).body(Map.of("error", "Authentication failed"));

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


        if (jwtUtil.isTokenExpired(refreshToken))
            return ResponseEntity.status(401).body(Map.of("error", "Refresh token expired"));

        String newAccessToken = jwtUtil.generateAccessToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);

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

    // ---------------------- LOGOUT ----------------------
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refreshToken".equals(c.getName())) {
                    // مسح الكوكيز بس
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

    // ---------------------- PROFILE ----------------------
    @GetMapping("/profile")
    public ResponseEntity<?> profile(
            @AuthenticationPrincipal String email
    ) {

        if (email == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "message", "Welcome " + user.getUsername()
        ));

    }
}
