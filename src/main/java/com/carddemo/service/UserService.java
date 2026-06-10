package com.carddemo.service;

import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserSecurityRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserSecurityRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UserSecurity> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public UserSecurity findById(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    @Transactional
    public UserSecurity create(UserSecurity user) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required for new users");
        }
        if (user.getUserId() != null && userRepository.existsById(user.getUserId())) {
            throw new IllegalArgumentException("User already exists with id: " + user.getUserId());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public UserSecurity update(String userId, UserSecurity updated) {
        UserSecurity existing = findById(userId);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setUserType(updated.getUserType());
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updated.getPassword()));
        }
        return userRepository.save(existing);
    }

    @Transactional
    public void delete(String userId) {
        UserSecurity existing = findById(userId);
        userRepository.delete(existing);
    }
}
