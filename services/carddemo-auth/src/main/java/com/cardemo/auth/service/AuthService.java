package com.cardemo.auth.service;

import com.cardemo.auth.dto.LoginRequest;
import com.cardemo.auth.dto.LoginResponse;
import com.cardemo.auth.entity.User;
import com.cardemo.auth.repository.UserRepository;
import com.cardemo.auth.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public LoginResponse authenticate(LoginRequest request) {
        User user = userRepository.findByUserId(request.userId())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String token = tokenProvider.generateToken(user.getUserId(), user.getUserType().name());
        return new LoginResponse(token, user.getUserId(), user.getUserType().name());
    }

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
