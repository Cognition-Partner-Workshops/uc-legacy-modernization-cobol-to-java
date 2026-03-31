package com.carddemo.service;

import com.carddemo.model.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserSecurityRepository userSecurityRepository;

    public AuthenticationService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    public Optional<UserSecurity> authenticate(String userId, String password) {
        return userSecurityRepository.findById(userId.toUpperCase())
                .filter(user -> user.getPassword().equals(password));
    }

    public boolean isAdmin(UserSecurity user) {
        return "A".equals(user.getUserType());
    }
}
