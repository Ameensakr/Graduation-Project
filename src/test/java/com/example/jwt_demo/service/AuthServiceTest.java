package com.example.jwt_demo.service;

import com.example.jwt_demo.dto.ResetPasswordRequest;
import com.example.jwt_demo.model.OtpToken;
import com.example.jwt_demo.model.User;
import com.example.jwt_demo.repository.OtpTokenRepository;
import com.example.jwt_demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock EmailService emailService;
    @Mock OtpTokenRepository otpTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AuthService authService;

    @Test
    void requestPasswordReset_savesOtp_andSendsEmail() {
        User u = new User("a", "a@b.com", "x");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(u));

        authService.requestPasswordReset("a@b.com");

        ArgumentCaptor<OtpToken> captor = ArgumentCaptor.forClass(OtpToken.class);
        verify(otpTokenRepository).save(captor.capture());
        OtpToken saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("a@b.com");
        assertThat(saved.getOtp()).hasSize(6).matches("\\d{6}");
        assertThat(saved.getExpirationTime()).isAfter(LocalDateTime.now().plusMinutes(9));

        verify(emailService).sendOtpEmail(eq("a@b.com"), eq(saved.getOtp()));
    }

    @Test
    void requestPasswordReset_nullEmail_throws() {
        assertThatThrownBy(() -> authService.requestPasswordReset(null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(otpTokenRepository, emailService);
    }

    @Test
    void requestPasswordReset_emptyEmail_throws() {
        assertThatThrownBy(() -> authService.requestPasswordReset(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requestPasswordReset_unknownUser_throws_andNoEmail() {
        when(userRepository.findByEmail("x@y.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.requestPasswordReset("x@y.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(otpTokenRepository, never()).save(any());
        verify(emailService, never()).sendOtpEmail(any(), any());
    }

    @Test
    void resetPassword_validOtp_updatesPassword_andDeletesOtp() {
        OtpToken token = new OtpToken("a@b.com", "123456", LocalDateTime.now().plusMinutes(5));
        User user = new User("a", "a@b.com", "old");

        when(otpTokenRepository.findByEmailAndOtp("a@b.com", "123456")).thenReturn(Optional.of(token));
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("NEW_HASH");

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail("a@b.com");
        req.setOtp("123456");
        req.setNewPassword("newPass");

        authService.resetPassword(req);

        assertThat(user.getPassword()).isEqualTo("NEW_HASH");
        verify(userRepository).save(user);
        verify(otpTokenRepository).delete(token);
    }

    @Test
    void resetPassword_missingFields_throws() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resetPassword_invalidOtp_throws() {
        when(otpTokenRepository.findByEmailAndOtp("a@b.com", "000000")).thenReturn(Optional.empty());

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail("a@b.com");
        req.setOtp("000000");
        req.setNewPassword("p");

        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid OTP");
    }

    @Test
    void resetPassword_expiredOtp_throws_andDeletesToken() {
        OtpToken token = new OtpToken("a@b.com", "123456", LocalDateTime.now().minusMinutes(1));
        when(otpTokenRepository.findByEmailAndOtp("a@b.com", "123456")).thenReturn(Optional.of(token));

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail("a@b.com");
        req.setOtp("123456");
        req.setNewPassword("p");

        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expired");

        verify(otpTokenRepository).delete(token);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_userMissing_throws() {
        OtpToken token = new OtpToken("a@b.com", "123456", LocalDateTime.now().plusMinutes(5));
        when(otpTokenRepository.findByEmailAndOtp("a@b.com", "123456")).thenReturn(Optional.of(token));
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setEmail("a@b.com");
        req.setOtp("123456");
        req.setNewPassword("p");

        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }
}
