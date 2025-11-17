package com.example.jwt_demo.service;

import com.example.jwt_demo.model.User;
import com.example.jwt_demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }

    public boolean registerUser(String username, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) return false;

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username, email, hashedPassword);
        userRepository.save(user);
        return true;
    }

    public boolean loginUserByEmail(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;
        return passwordEncoder.matches(password, userOpt.get().getPassword());
    }

    // الآن refresh token صار JWT، مش random string
    public void storeRefreshTokenForUser(String email, String refreshToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        user.setRefreshTokenHash(passwordEncoder.encode(refreshToken));
        user.setRefreshTokenExpiry(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)); // 7 أيام
        userRepository.save(user);
    }

    public boolean validateRefreshToken(String email, String refreshToken) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        if (user.getRefreshTokenHash() == null) return false;
        if (user.getRefreshTokenExpiry() == null || user.getRefreshTokenExpiry().before(new Date())) return false;

        return passwordEncoder.matches(refreshToken, user.getRefreshTokenHash());
    }

    public void revokeRefreshToken(String email) {
        userRepository.findByEmail(email).ifPresent(u -> {
            u.setRefreshTokenHash(null);
            u.setRefreshTokenExpiry(null);
            userRepository.save(u);
        });
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
