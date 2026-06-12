package com.cardemo.auth.controller;

import com.cardemo.auth.dto.CreateUserRequest;
import com.cardemo.auth.dto.UpdateUserRequest;
import com.cardemo.auth.dto.UserResponse;
import com.cardemo.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(Authentication authentication) {
        if (isAdmin(authentication)) {
            return ResponseEntity.ok(userService.getAllUsers());
        }
        // Regular users can only see their own profile
        String userId = authentication.getName();
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(List.of(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id,
                                                   @Valid @RequestBody UpdateUserRequest request,
                                                   Authentication authentication) {
        if (!isAdmin(authentication) && !authentication.getName().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // Non-admin users cannot change their own role
        if (!isAdmin(authentication) && request.userType() != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
