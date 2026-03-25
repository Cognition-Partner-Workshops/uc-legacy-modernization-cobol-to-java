package com.cardemo.controller;

import com.cardemo.model.SecurityUser;
import com.cardemo.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

import java.util.Map;

/**
 * User Management Controller - converted from COBOL programs COUSR00C-COUSR03C
 * Original: CICS Admin screens for User List, Add, Update, Delete
 * Replaces BMS maps COUSR00/01/02/03 with REST endpoints.
 * Admin-only operations (original COADM01C admin menu).
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/admin/users?page={page}&size={size}
     * List users with pagination - replaces COUSR00C.
     * Supports PF7/PF8 page forward/backward via page parameter.
     */
    @GetMapping
    public ResponseEntity<Page<SecurityUser>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.listUsers(PageRequest.of(page, size)));
    }

    /**
     * GET /api/admin/users/{userId}
     * Get user details.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<SecurityUser> getUser(@PathVariable String userId) {
        return userService.getUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/admin/users
     * Add a new user - replaces COUSR01C PROCESS-ENTER-KEY.
     */
    @PostMapping
    public ResponseEntity<Object> addUser(@RequestBody SecurityUser user) {
        try {
            SecurityUser saved = userService.addUser(user);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/users/{userId}
     * Update a user - replaces COUSR02C PROCESS-ENTER-KEY.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<SecurityUser> updateUser(@PathVariable String userId,
                                                   @RequestBody SecurityUser updatedData) {
        return userService.updateUser(userId, updatedData)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/admin/users/{userId}
     * Delete a user - replaces COUSR03C PROCESS-ENTER-KEY.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        if (userService.deleteUser(userId)) {
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
