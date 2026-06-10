package com.carddemo.controller;

import com.carddemo.model.UserSecurity;
import com.carddemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<UserSecurity> listUsers(Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/{userId}")
    public UserSecurity getUser(@PathVariable String userId) {
        return userService.findById(userId);
    }

    @PostMapping
    public ResponseEntity<UserSecurity> createUser(@Valid @RequestBody UserSecurity user) {
        UserSecurity created = userService.create(user);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{userId}")
    public UserSecurity updateUser(@PathVariable String userId, @Valid @RequestBody UserSecurity user) {
        return userService.update(userId, user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
