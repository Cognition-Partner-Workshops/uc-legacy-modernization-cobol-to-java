package com.cardemo.service;

import com.cardemo.model.SecurityUser;
import com.cardemo.repository.SecurityUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * User Management Service - converted from COBOL programs COUSR00C, COUSR01C, COUSR02C, COUSR03C
 * Original: CICS Admin screens for User List, Add, Update, Delete
 * These are admin-only operations (CDEMO-USRTYP-ADMIN = 'A').
 */
@Service
public class UserService {

    private final SecurityUserRepository securityUserRepository;

    public UserService(SecurityUserRepository securityUserRepository) {
        this.securityUserRepository = securityUserRepository;
    }

    /**
     * List all users with pagination - equivalent to COUSR00C (User List).
     */
    public Page<SecurityUser> listUsers(Pageable pageable) {
        return securityUserRepository.findAll(pageable);
    }

    /**
     * Get a specific user by ID.
     */
    public Optional<SecurityUser> getUser(String userId) {
        return securityUserRepository.findById(userId.toUpperCase().trim());
    }

    /**
     * Add a new user - equivalent to COUSR01C (User Add).
     */
    @Transactional
    public SecurityUser addUser(SecurityUser user) {
        String userId = user.getUserId().toUpperCase().trim();
        if (securityUserRepository.existsById(userId)) {
            throw new IllegalArgumentException("User already exists: " + userId);
        }
        user.setUserId(userId);
        if (user.getPassword() != null) {
            user.setPassword(user.getPassword().toUpperCase().trim());
        }
        return securityUserRepository.save(user);
    }

    /**
     * Update an existing user - equivalent to COUSR02C (User Update).
     */
    @Transactional
    public Optional<SecurityUser> updateUser(String userId, SecurityUser updatedData) {
        return securityUserRepository.findById(userId.toUpperCase().trim()).map(existing -> {
            if (updatedData.getFirstName() != null) {
                existing.setFirstName(updatedData.getFirstName());
            }
            if (updatedData.getLastName() != null) {
                existing.setLastName(updatedData.getLastName());
            }
            if (updatedData.getPassword() != null) {
                existing.setPassword(updatedData.getPassword().toUpperCase().trim());
            }
            if (updatedData.getUserType() != null) {
                existing.setUserType(updatedData.getUserType());
            }
            return securityUserRepository.save(existing);
        });
    }

    /**
     * Delete a user - equivalent to COUSR03C (User Delete).
     */
    @Transactional
    public boolean deleteUser(String userId) {
        String normalizedId = userId.toUpperCase().trim();
        if (securityUserRepository.existsById(normalizedId)) {
            securityUserRepository.deleteById(normalizedId);
            return true;
        }
        return false;
    }
}
