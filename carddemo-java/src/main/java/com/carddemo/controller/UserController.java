package com.carddemo.controller;

import com.carddemo.dto.PagedResponse;
import com.carddemo.dto.UserCreateRequest;
import com.carddemo.dto.UserResponse;
import com.carddemo.dto.UserUpdateRequest;
import com.carddemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller replacing COBOL CICS transactions CU00–CU03.
 * All endpoints are admin-only (secured in SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** GET /api/admin/users?page=0&size=10  — COUSR00C list */
    @GetMapping
    public ResponseEntity<PagedResponse<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.listUsers(page, size));
    }

    /** POST /api/admin/users — COUSR01C add */
    @PostMapping
    public ResponseEntity<UserResponse> addUser(
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.addUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** GET /api/admin/users/{userId} — COUSR02C read */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    /** PUT /api/admin/users/{userId} — COUSR02C write */
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    /** DELETE /api/admin/users/{userId} — COUSR03C delete */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
