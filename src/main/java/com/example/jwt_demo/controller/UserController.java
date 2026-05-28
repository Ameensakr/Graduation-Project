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

import com.example.jwt_demo.repository.SavedPlanRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
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
    @Autowired
    private SavedPlanRepository savedPlanRepository;

    @Autowired
    private com.example.jwt_demo.service.BlobStorageService blobStorageService;

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
                cookie.setSecure(true);
                cookie.setPath("/");
                cookie.setMaxAge(7 * 24 * 60 * 60);
                response.addCookie(cookie);

                return ResponseEntity.ok(Map.of(
                        "accessToken", accessToken,
                        "refreshToken", refreshToken,
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
    public ResponseEntity<?> refresh(@RequestBody(required = false) Map<String, String> body, HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        // 1. Try to get it from the JSON body (for mobile)
        if (body != null && body.containsKey("refreshToken")) {
            refreshToken = body.get("refreshToken");
        }

        // 2. Fallback to cookies (for web)
        if (refreshToken == null && request.getCookies() != null) {
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
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken,
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

        long plansMade = savedPlanRepository.countByUserId(user.getId());

        Map<String, Object> body = new HashMap<>();
        body.put("username", user.getUsername());
        body.put("email", user.getEmail());
        body.put("userImage", user.getUserImage());
        body.put("bio", user.getBio());
        body.put("adventureLevel", null);
        body.put("plansMade", plansMade);
        body.put("sitesVisited", 0);
        body.put("nightsBooked", 0);
        return ResponseEntity.ok(body);

    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal String email,
            @RequestBody Map<String, String> body) {
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        User user = userOpt.get();
        if (body.containsKey("bio")) user.setBio(body.get("bio"));
        if (body.containsKey("userImage")) {
            String newImage = body.get("userImage");
            String oldImage = user.getUserImage();
            if (oldImage != null && !oldImage.equals(newImage)) {
                blobStorageService.deleteByUrl(oldImage);
            }
            user.setUserImage(newImage);
        }
        if (body.containsKey("username")) user.setUsername(body.get("username"));
        userService.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }
}
