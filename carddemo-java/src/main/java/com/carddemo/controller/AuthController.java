package com.carddemo.controller;

import com.carddemo.dto.ErrorResponse;
import com.carddemo.dto.SignInRequest;
import com.carddemo.dto.SignInResponse;
import com.carddemo.entity.User;
import com.carddemo.repository.UserRepository;
import com.carddemo.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String SIGN_IN_ERROR_MESSAGE =
            "Sign in Unsuccessful. Userid / Password does not match.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest request) {
        String userId = request.getUserId().toUpperCase().trim();

        Optional<User> userOpt = userRepository.findByUserId(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(SIGN_IN_ERROR_MESSAGE));
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(SIGN_IN_ERROR_MESSAGE));
        }

        String token = jwtUtil.generateToken(
                user.getUserId(),
                user.getUserType(),
                user.getFirstName(),
                user.getLastName()
        );

        SignInResponse response = SignInResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType())
                .build();

        return ResponseEntity.ok(response);
    }
}
