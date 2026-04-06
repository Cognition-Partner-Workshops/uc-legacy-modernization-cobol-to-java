package com.suitecrm.auth.service;

import com.suitecrm.auth.dto.AuthResponse;
import com.suitecrm.auth.dto.LoginRequest;
import com.suitecrm.auth.dto.UserDto;
import com.suitecrm.auth.entity.Permission;
import com.suitecrm.auth.entity.Role;
import com.suitecrm.auth.entity.User;
import com.suitecrm.auth.repository.RoleRepository;
import com.suitecrm.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameAndDeletedFalse(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if ("Inactive".equals(user.getStatus())) {
            throw new RuntimeException("User account is inactive");
        }

        if ("Locked".equals(user.getStatus())) {
            // Auto-unlock after 30 minutes
            if (user.getDateModified() != null &&
                    user.getDateModified().plusMinutes(30).isBefore(LocalDateTime.now())) {
                user.setStatus("Active");
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            } else {
                throw new RuntimeException("User account is locked. Please try again later.");
            }
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setStatus("Locked");
            }
            userRepository.save(user);
            throw new RuntimeException("Invalid username or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getModuleName() + ":" + p.getActionName())
                .collect(Collectors.toSet());

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), roleNames, permissions);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roles(roleNames)
                .permissions(permissions)
                .build();
    }

    @Transactional
    public UserDto registerUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .passwordHash(passwordEncoder.encode(userDto.getPassword()))
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .phoneWork(userDto.getPhoneWork())
                .phoneMobile(userDto.getPhoneMobile())
                .title(userDto.getTitle())
                .department(userDto.getDepartment())
                .status("Active")
                .isAdmin(false)
                .timezone(userDto.getTimezone() != null ? userDto.getTimezone() : "UTC")
                .language(userDto.getLanguage() != null ? userDto.getLanguage() : "en_US")
                .build();

        Role defaultRole = roleRepository.findByNameAndDeletedFalse("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.getRoles().add(defaultRole);

        User savedUser = userRepository.save(user);
        log.info("User registered: {}", savedUser.getUsername());

        return mapToDto(savedUser);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, username)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getModuleName() + ":" + p.getActionName())
                .collect(Collectors.toSet());

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), roleNames, permissions);
        String newRefreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roles(roleNames)
                .permissions(permissions)
                .build();
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneWork(user.getPhoneWork())
                .phoneMobile(user.getPhoneMobile())
                .title(user.getTitle())
                .department(user.getDepartment())
                .status(user.getStatus())
                .isAdmin(user.getIsAdmin())
                .timezone(user.getTimezone())
                .language(user.getLanguage())
                .dateEntered(user.getDateEntered())
                .dateModified(user.getDateModified())
                .lastLogin(user.getLastLogin())
                .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
