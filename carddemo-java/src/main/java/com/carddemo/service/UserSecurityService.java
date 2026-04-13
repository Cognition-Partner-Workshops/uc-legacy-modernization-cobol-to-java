package com.carddemo.service;

import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSecurityService {

    private final UserSecurityRepository userSecurityRepository;

    public UserSecurityService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    /**
     * List users - mirrors COUSR00C.cbl
     */
    public Page<UserSecurity> listUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userSecurityRepository.findAll(pageable);
    }

    /**
     * Add user - mirrors COUSR01C.cbl
     * Validates no duplicate user IDs
     */
    @Transactional
    public UserSecurity addUser(UserSecurity user) {
        if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        String userId = user.getUserId().toUpperCase().trim();
        if (userSecurityRepository.existsById(userId)) {
            throw new IllegalArgumentException("User already exists: " + userId);
        }

        user.setUserId(userId);
        user.setPassword(user.getPassword().toUpperCase().trim());

        if (user.getUserType() == null || user.getUserType().trim().isEmpty()) {
            user.setUserType("U");
        }

        return userSecurityRepository.save(user);
    }

    /**
     * Update user - mirrors COUSR02C.cbl
     */
    @Transactional
    public UserSecurity updateUser(String userId, UserSecurity updatedFields) {
        UserSecurity existing = userSecurityRepository.findById(userId.toUpperCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (updatedFields.getPassword() != null && !updatedFields.getPassword().trim().isEmpty()) {
            existing.setPassword(updatedFields.getPassword().toUpperCase().trim());
        }
        if (updatedFields.getUserType() != null && !updatedFields.getUserType().trim().isEmpty()) {
            existing.setUserType(updatedFields.getUserType());
        }
        if (updatedFields.getFirstName() != null) {
            existing.setFirstName(updatedFields.getFirstName());
        }
        if (updatedFields.getLastName() != null) {
            existing.setLastName(updatedFields.getLastName());
        }

        return userSecurityRepository.save(existing);
    }

    /**
     * Delete user - mirrors COUSR03C.cbl
     */
    @Transactional
    public void deleteUser(String userId) {
        String upperUserId = userId.toUpperCase().trim();
        if (!userSecurityRepository.existsById(upperUserId)) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        userSecurityRepository.deleteById(upperUserId);
    }

    /**
     * Get user by ID
     */
    public UserSecurity getUser(String userId) {
        return userSecurityRepository.findById(userId.toUpperCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
