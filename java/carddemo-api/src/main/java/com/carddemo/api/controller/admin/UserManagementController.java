package com.carddemo.api.controller.admin;

import com.carddemo.api.service.UserManagementService;
import com.carddemo.common.model.UserSecurity;
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
 * User Management controller replacing COBOL programs:
 * - COUSR00C (CU00 transaction) — User List
 * - COUSR01C (CU01 transaction) — User Add
 * - COUSR02C (CU02 transaction) — User Update
 * - COUSR03C (CU03 transaction) — User Delete
 *
 * Original COBOL: app/cbl/COUSR00C.cbl, app/cbl/COUSR01C.cbl,
 *                 app/cbl/COUSR02C.cbl, app/cbl/COUSR03C.cbl
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    /**
     * GET /api/admin/users — List all users.
     * Replaces COUSR00C (CU00 transaction).
     */
    @GetMapping
    public ResponseEntity<List<UserSecurity>> listUsers() {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COUSR00C");
    }

    /**
     * GET /api/admin/users/{userId} — Get user by ID.
     * Replaces part of COUSR02C user lookup.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserSecurity> getUser(@PathVariable String userId) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COUSR02C");
    }

    /**
     * POST /api/admin/users — Create a new user.
     * Replaces COUSR01C (CU01 transaction).
     */
    @PostMapping
    public ResponseEntity<UserSecurity> createUser(@RequestBody UserSecurity user) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COUSR01C");
    }

    /**
     * PUT /api/admin/users/{userId} — Update an existing user.
     * Replaces COUSR02C (CU02 transaction).
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserSecurity> updateUser(
            @PathVariable String userId,
            @RequestBody UserSecurity user) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COUSR02C");
    }

    /**
     * DELETE /api/admin/users/{userId} — Delete a user.
     * Replaces COUSR03C (CU03 transaction).
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COUSR03C");
    }
}
