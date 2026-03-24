package com.carddemo.usermanagement.controller;

import com.carddemo.usermanagement.dto.CreateUserRequest;
import com.carddemo.usermanagement.dto.UpdateUserRequest;
import com.carddemo.usermanagement.dto.UserResponse;
import com.carddemo.usermanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST controller replacing the four COBOL Admin User Management programs.
 *
 * <table>
 *   <tr><th>HTTP Method</th><th>Endpoint</th><th>Replaces COBOL Program</th></tr>
 *   <tr><td>GET</td><td>/api/users</td><td>COUSR00C (List users)</td></tr>
 *   <tr><td>GET</td><td>/api/users/{userId}</td><td>READ in COUSR02C/COUSR03C</td></tr>
 *   <tr><td>POST</td><td>/api/users</td><td>COUSR01C (Add user)</td></tr>
 *   <tr><td>PUT</td><td>/api/users/{userId}</td><td>COUSR02C (Update user)</td></tr>
 *   <tr><td>DELETE</td><td>/api/users/{userId}</td><td>COUSR03C (Delete user)</td></tr>
 * </table>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * List users with pagination. Replaces COUSR00C's sequential VSAM browse.
     * The COBOL program displays 10 users per page with PF7 (backward) and PF8 (forward).
     *
     * @param page zero-based page number (default 0)
     * @param size page size (default 10, matching COBOL's 10-row display)
     * @return paginated list of users
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.listUsers(page, size));
    }

    /**
     * Get a single user by ID. Mirrors the READ operation in COUSR02C/COUSR03C.
     *
     * @param userId the user ID to look up
     * @return the user details (404 if not found)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    /**
     * Create a new user. Replaces COUSR01C's WRITE to USRSEC.
     * Returns 201 on success with a message matching the COBOL original:
     * "User {id} has been added ..."
     *
     * @param request the user creation payload
     * @return the created user with a success message (409 if duplicate)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.createUser(request);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "User " + created.getUserId() + " has been added ...");
        response.put("user", created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing user. Replaces COUSR02C's REWRITE to USRSEC.
     * Returns a message matching the COBOL original: "User {id} has been updated ..."
     *
     * @param userId  the user ID from the path
     * @param request the update payload
     * @return the updated user with a success message (404 if not found)
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse updated = userService.updateUser(userId, request);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "User " + updated.getUserId() + " has been updated ...");
        response.put("user", updated);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a user. Replaces COUSR03C's DELETE from USRSEC.
     * Returns a message matching the COBOL original: "User {id} has been deleted ..."
     *
     * @param userId the user ID to delete
     * @return success message (404 if not found)
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "User " + userId + " has been deleted ...");
        return ResponseEntity.ok(response);
    }
}
