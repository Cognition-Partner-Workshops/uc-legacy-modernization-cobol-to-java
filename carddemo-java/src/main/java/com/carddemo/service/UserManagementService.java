package com.carddemo.service;

import com.carddemo.dto.UserDTO;
import com.carddemo.dto.UserResponse;
import com.carddemo.entity.User;
import com.carddemo.exception.DuplicateResourceException;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User management service - consolidates COUSR00C (list), COUSR01C (add),
 * COUSR02C (update), COUSR03C (delete) into a single service.
 * Eliminates duplicate VSAM I/O patterns across the four COBOL programs.
 */
@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * List users with pagination - replaces COUSR00C's STARTBR/READNEXT/READPREV pattern.
     */
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAllByOrderByUserIdAsc(pageable)
                .map(UserResponse::fromEntity);
    }

    /**
     * Get a single user - replaces COUSR02C/COUSR03C's initial READ operation.
     */
    public UserResponse getUser(String userId) {
        User user = findUserOrThrow(userId);
        return UserResponse.fromEntity(user);
    }

    /**
     * Create a new user - replaces COUSR01C's WRITE operation.
     * Validates no duplicate key (DFHRESP(DUPREC) in COBOL).
     */
    @Transactional
    public UserResponse createUser(UserDTO dto) {
        if (userRepository.existsByUserId(dto.getUserId())) {
            throw new DuplicateResourceException("User", dto.getUserId());
        }

        User user = new User();
        user.setUserId(dto.getUserId());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setUserType(dto.getUserType());

        userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    /**
     * Update an existing user - replaces COUSR02C's READ/REWRITE pattern.
     */
    @Transactional
    public UserResponse updateUser(String userId, UserDTO dto) {
        User user = findUserOrThrow(userId);

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUserType(dto.getUserType());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    /**
     * Delete a user - replaces COUSR03C's READ/DELETE pattern.
     */
    @Transactional
    public void deleteUser(String userId) {
        User user = findUserOrThrow(userId);
        userRepository.delete(user);
    }

    private User findUserOrThrow(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
