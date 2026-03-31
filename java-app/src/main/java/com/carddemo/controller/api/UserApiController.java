package com.carddemo.controller.api;

import com.carddemo.dto.UserCreateRequest;
import com.carddemo.model.UserSecurity;
import com.carddemo.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserApiController {

    private final UserManagementService userManagementService;

    public UserApiController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public ResponseEntity<List<UserSecurity>> listUsers() {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserSecurity> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(userManagementService.getUser(userId));
    }

    @PostMapping
    public ResponseEntity<UserSecurity> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userManagementService.createUser(request));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserSecurity> updateUser(@PathVariable String userId, @Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userManagementService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userManagementService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
