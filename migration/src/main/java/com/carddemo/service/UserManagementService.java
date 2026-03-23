package com.carddemo.service;

import com.carddemo.dto.UserRequest;
import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Replaces COUSR00C (list), COUSR01C (add), COUSR02C (update), COUSR03C (delete).
 */
@Service
public class UserManagementService {

    private final UserSecurityRepository userSecurityRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserSecurityRepository userSecurityRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userSecurityRepository = userSecurityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserSecurity> getAllUsers() {
        return userSecurityRepository.findAll();
    }

    public UserSecurity getUser(String userId) {
        return userSecurityRepository.findById(userId.toUpperCase())
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    @Transactional
    public UserSecurity addUser(UserRequest request) {
        String userId = request.getUserId().toUpperCase().trim();
        if (userSecurityRepository.existsById(userId)) {
            throw new RuntimeException("User already exists: " + userId);
        }

        UserSecurity user = new UserSecurity();
        user.setUserId(userId);
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword().toUpperCase().trim()));
        user.setUserType(request.getUserType().toUpperCase().trim());

        return userSecurityRepository.save(user);
    }

    @Transactional
    public UserSecurity updateUser(String userId, UserRequest request) {
        UserSecurity user = getUser(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword().toUpperCase().trim()));
        }
        if (request.getUserType() != null) {
            user.setUserType(request.getUserType().toUpperCase().trim());
        }

        return userSecurityRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        UserSecurity user = getUser(userId);
        userSecurityRepository.delete(user);
    }
}
