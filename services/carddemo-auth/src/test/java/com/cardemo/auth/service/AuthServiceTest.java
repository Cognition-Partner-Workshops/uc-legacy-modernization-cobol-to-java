package com.cardemo.auth.service;

import com.cardemo.auth.dto.LoginRequest;
import com.cardemo.auth.dto.LoginResponse;
import com.cardemo.auth.entity.User;
import com.cardemo.auth.entity.User.UserType;
import com.cardemo.auth.repository.UserRepository;
import com.cardemo.auth.security.JwtTokenProvider;
import com.cardemo.auth.service.AuthService.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, tokenProvider);
    }

    @Test
    void authenticate_withValidCredentials_returnsToken() {
        User user = new User("ADMIN01", "John", "Doe", "encoded_pwd", UserType.ADMIN);
        when(userRepository.findByUserId("ADMIN01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded_pwd")).thenReturn(true);
        when(tokenProvider.generateToken("ADMIN01", "ADMIN")).thenReturn("jwt-token");

        LoginResponse response = authService.authenticate(new LoginRequest("ADMIN01", "password"));

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("ADMIN01", response.userId());
        assertEquals("ADMIN", response.userType());
    }

    @Test
    void authenticate_withInvalidUserId_throwsException() {
        when(userRepository.findByUserId("INVALID")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> authService.authenticate(new LoginRequest("INVALID", "password")));
    }

    @Test
    void authenticate_withWrongPassword_throwsException() {
        User user = new User("USER01", "Jane", "Doe", "encoded_pwd", UserType.USER);
        when(userRepository.findByUserId("USER01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(AuthenticationException.class,
                () -> authService.authenticate(new LoginRequest("USER01", "wrong")));
    }
}
