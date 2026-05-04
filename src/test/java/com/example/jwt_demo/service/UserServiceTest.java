package com.example.jwt_demo.service;

import com.example.jwt_demo.model.User;
import com.example.jwt_demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    private User existing;

    @BeforeEach
    void setUp() {
        existing = new User("alice", "a@b.com", "hashed");
        existing.setId("1");
    }

    @Test
    void registerUser_newEmail_savesHashedPassword() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain")).thenReturn("HASHED");

        boolean ok = userService.registerUser("alice", "a@b.com", "plain");

        assertThat(ok).isTrue();
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("HASHED");
        assertThat(captor.getValue().getEmail()).isEqualTo("a@b.com");
    }

    @Test
    void registerUser_existingEmail_returnsFalse_andNoSave() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(existing));

        boolean ok = userService.registerUser("alice", "a@b.com", "plain");

        assertThat(ok).isFalse();
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void loadUserByUsername_existing_returnsUserDetails() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(existing));

        UserDetails ud = userService.loadUserByUsername("a@b.com");

        assertThat(ud.getUsername()).isEqualTo("a@b.com");
        assertThat(ud.getPassword()).isEqualTo("hashed");
        assertThat(ud.getAuthorities()).extracting("authority").contains("USER");
    }

    @Test
    void loadUserByUsername_missing_throws() {
        when(userRepository.findByEmail("missing@b.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("missing@b.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void findByEmail_delegates() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(existing));
        assertThat(userService.findByEmail("a@b.com")).contains(existing);
    }
}
