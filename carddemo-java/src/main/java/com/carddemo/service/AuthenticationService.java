package com.carddemo.service;

import com.carddemo.config.JwtUtil;
import com.carddemo.dto.LoginRequest;
import com.carddemo.dto.LoginResponse;
import com.carddemo.entity.User;
import com.carddemo.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication service - replaces COSGN00C sign-on program.
 * Original COBOL: reads USRSEC file, compares plaintext password, routes by user type.
 * Java: uses BCrypt password hashing and JWT token generation.
 */
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthenticationService(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse authenticate(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new BadCredentialsException("Invalid user ID or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid user ID or password");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUserType());

        return new LoginResponse(
                token,
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserType()
        );
    }
}
