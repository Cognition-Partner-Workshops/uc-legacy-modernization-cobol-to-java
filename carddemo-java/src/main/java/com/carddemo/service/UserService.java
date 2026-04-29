package com.carddemo.service;

import com.carddemo.dto.PagedResponse;
import com.carddemo.dto.UserCreateRequest;
import com.carddemo.dto.UserResponse;
import com.carddemo.dto.UserUpdateRequest;
import com.carddemo.entity.User;
import com.carddemo.exception.UserAlreadyExistsException;
import com.carddemo.exception.UserNotFoundException;
import com.carddemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Replaces COBOL programs COUSR00C (list), COUSR01C (add),
 * COUSR02C (update), and COUSR03C (delete).
 *
 * CICS STARTBR/READNEXT pagination → Spring Data Page.
 * CICS READ duplicate check → existsByUserId().
 * CICS WRITE/REWRITE/DELETE → JPA save/delete.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** COUSR00C — paginated list via STARTBR/READNEXT equivalent */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> listUsers(int page, int size) {
        Page<User> userPage = userRepository.findAll(
                PageRequest.of(page, size, Sort.by("userId")));
        List<UserResponse> content = userPage.getContent().stream()
                .map(this::toResponse)
                .toList();
        return new PagedResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages());
    }

    /** COUSR01C — add user (CICS READ check + WRITE) */
    @Transactional
    public UserResponse addUser(UserCreateRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new UserAlreadyExistsException(request.getUserId());
        }
        User user = new User();
        user.setUserId(request.getUserId());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(request.getUserType());
        userRepository.save(user);
        return toResponse(user);
    }

    /** COUSR02C — read user for update form */
    @Transactional(readOnly = true)
    public UserResponse getUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return toResponse(user);
    }

    /** COUSR02C — update user (CICS REWRITE) */
    @Transactional
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setUserType(request.getUserType());
        userRepository.save(user);
        return toResponse(user);
    }

    /** COUSR03C — delete user (CICS DELETE) */
    @Transactional
    public void deleteUser(String userId) {
        if (!userRepository.existsByUserId(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserType());
    }
}
