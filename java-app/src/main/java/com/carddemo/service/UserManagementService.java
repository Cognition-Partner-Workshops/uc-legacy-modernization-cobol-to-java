package com.carddemo.service;

import com.carddemo.dto.UserCreateRequest;
import com.carddemo.exception.CardDemoException;
import com.carddemo.model.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserManagementService {

    private final UserSecurityRepository userSecurityRepository;

    public UserManagementService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    public List<UserSecurity> getAllUsers() {
        return userSecurityRepository.findAllByOrderByUserIdAsc();
    }

    public Optional<UserSecurity> findById(String userId) {
        return userSecurityRepository.findById(userId.toUpperCase());
    }

    public UserSecurity getUser(String userId) {
        return userSecurityRepository.findById(userId.toUpperCase())
                .orElseThrow(() -> new CardDemoException("User not found: " + userId));
    }

    @Transactional
    public UserSecurity createUser(UserCreateRequest request) {
        String userId = request.getUserId().toUpperCase();
        if (userSecurityRepository.existsById(userId)) {
            throw new CardDemoException("User already exists: " + userId);
        }

        UserSecurity user = UserSecurity.builder()
                .userId(userId)
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .userType(request.getUserType().toUpperCase())
                .build();

        return userSecurityRepository.save(user);
    }

    @Transactional
    public UserSecurity updateUser(String userId, UserCreateRequest request) {
        UserSecurity user = getUser(userId);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(request.getPassword());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getUserType() != null) {
            user.setUserType(request.getUserType().toUpperCase());
        }

        return userSecurityRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        UserSecurity user = getUser(userId);
        userSecurityRepository.delete(user);
    }
}
