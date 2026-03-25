package com.cardemo.controller;

import com.cardemo.model.UserSecurityRecord;
import com.cardemo.service.online.UserManagementService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * User Controller - REST API for user management operations.
 * Migrated from COBOL CICS programs COUSR00C through COUSR03C.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public ResponseEntity<Page<UserSecurityRecord>> listUsers(
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(userManagementService.listUsers(page));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserSecurityRecord> getUser(@PathVariable String userId) {
        try {
            UserSecurityRecord user = userManagementService.getUser(userId);
            return ResponseEntity.ok(user);
        } catch (UserManagementService.UserManagementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addUser(
            @RequestParam String userId,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String password,
            @RequestParam String userType) {
        try {
            UserSecurityRecord saved = userManagementService.addUser(
                    userId, firstName, lastName, password, userType);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "User added successfully",
                    "user", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable String userId,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String password,
            @RequestParam String userType) {
        try {
            UserSecurityRecord updated = userManagementService.updateUser(
                    userId, firstName, lastName, password, userType);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "User updated successfully",
                    "user", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId) {
        try {
            userManagementService.deleteUser(userId);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }
}
