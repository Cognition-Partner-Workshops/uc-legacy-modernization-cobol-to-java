package com.carddemo.auth.controller;

import com.carddemo.auth.model.UserSecurity;
import com.carddemo.auth.repository.UserSecurityRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserSecurityRepository userSecurityRepository;

    public AuthController(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String userId = credentials.get("userId");
        String password = credentials.get("password");

        if (userId == null || userId.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing credentials"));
        }

        Optional<UserSecurity> userOpt = userSecurityRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "User not found"));
        }

        UserSecurity user = userOpt.get();
        if (user.getSecUsrPwd() == null || !user.getSecUsrPwd().equals(password)) {
            return ResponseEntity.status(401).body(Map.of("error", "Wrong password"));
        }

        return ResponseEntity.ok(Map.of(
            "userId", user.getSecUsrId(),
            "userType", user.getSecUsrType(),
            "firstName", user.getSecUsrFname() != null ? user.getSecUsrFname() : "",
            "lastName", user.getSecUsrLname() != null ? user.getSecUsrLname() : ""
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        return ResponseEntity.ok(userSecurityRepository.findAll());
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserSecurity user) {
        userSecurityRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody UserSecurity user) {
        if (!userSecurityRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        user.setSecUsrId(userId);
        userSecurityRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        if (!userSecurityRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        userSecurityRepository.deleteById(userId);
        return ResponseEntity.noContent().build();
    }
}
