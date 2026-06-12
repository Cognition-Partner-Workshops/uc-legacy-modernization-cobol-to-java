package com.cardemo.auth.service;

import com.cardemo.auth.dto.CreateUserRequest;
import com.cardemo.auth.dto.UpdateUserRequest;
import com.cardemo.auth.dto.UserResponse;
import com.cardemo.auth.entity.User;
import com.cardemo.auth.entity.User.UserType;
import com.cardemo.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getUserById(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        return UserResponse.from(user);
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUserId(request.userId())) {
            throw new UserAlreadyExistsException("User already exists: " + request.userId());
        }

        User user = new User(
                request.userId(),
                request.firstName(),
                request.lastName(),
                passwordEncoder.encode(request.password()),
                UserType.valueOf(request.userType())
        );

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.userType() != null) {
            user.setUserType(UserType.valueOf(request.userType()));
        }

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    public void deleteUser(String userId) {
        if (!userRepository.existsByUserId(userId)) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }
}
