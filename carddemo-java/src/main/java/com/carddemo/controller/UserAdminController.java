package com.carddemo.controller;

import com.carddemo.dto.UserCreateRequest;
import com.carddemo.dto.UserDto;
import com.carddemo.service.online.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * User administration controller replacing COBOL programs COUSR00C-COUSR03C.
 * Requires ROLE_ADMIN (replaces COADM01C admin menu check).
 *
 * <p>COUSR00C (List Users, CICS txn CU00) -> GET /api/admin/users
 * <p>COUSR01C (Add User, CICS txn CU01) -> POST /api/admin/users
 * <p>COUSR02C (Update User, CICS txn CU02) -> PUT /api/admin/users/{id}
 * <p>COUSR03C (Delete User, CICS txn CU03) -> DELETE /api/admin/users/{id}
 */
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "User Administration", description = "Admin user management (replaces COUSR00C-03C, requires ADMIN role)")
public class UserAdminController {

    private final UserManagementService userManagementService;

    public UserAdminController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    @Operation(summary = "List all users",
            description = "Browse all user records. Replaces COUSR00C (CICS txn CU00). "
                    + "Original uses STARTBR/READNEXT on USRSEC file.")
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(userManagementService.listUsers());
    }

    @PostMapping
    @Operation(summary = "Create new user",
            description = "Add a new user with BCrypt-hashed password. Replaces COUSR01C (CICS txn CU01). "
                    + "Original writes plain-text password to USRSEC.")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userManagementService.createUser(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user details",
            description = "Retrieve user by ID. Used by update/delete screens.")
    public ResponseEntity<UserDto> getUser(
            @Parameter(description = "User ID (SEC-USR-ID, PIC X(08))")
            @PathVariable String id) {
        return ResponseEntity.ok(userManagementService.getUser(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user",
            description = "Update user fields. Replaces COUSR02C (CICS txn CU02). "
                    + "Password is re-hashed if provided.")
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "User ID")
            @PathVariable String id,
            @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userManagementService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user",
            description = "Delete a user record. Replaces COUSR03C (CICS txn CU03). "
                    + "Original confirms deletion via BMS screen then deletes from USRSEC.")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID")
            @PathVariable String id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
