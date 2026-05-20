package com.carddemo.auth.service;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.model.User;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        tokenProvider = new JwtTokenProvider(
                "CardDemoSecretKeyForJWTTokenGenerationMustBeAtLeast256Bits!!",
                3600000
        );
        authService = new AuthService(userRepository, passwordEncoder, tokenProvider);
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        User user = User.builder()
                .userId("ADMIN001")
                .firstName("Admin")
                .lastName("User")
                .passwordHash(passwordEncoder.encode("PASSWORD"))
                .userType("A")
                .build();
        when(userRepository.findById("ADMIN001")).thenReturn(Optional.of(user));

        LoginResponse response = authService.login(new LoginRequest("ADMIN001", "PASSWORD"));

        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUserId()).isEqualTo("ADMIN001");
        assertThat(response.getUserType()).isEqualTo("A");
    }

    @Test
    void login_withWrongPassword_shouldThrow() {
        User user = User.builder()
                .userId("ADMIN001")
                .passwordHash(passwordEncoder.encode("PASSWORD"))
                .userType("A")
                .build();
        when(userRepository.findById("ADMIN001")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ADMIN001", "WRONG")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_withNonexistentUser_shouldThrow() {
        when(userRepository.findById("NOUSER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("NOUSER", "PASSWORD")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
