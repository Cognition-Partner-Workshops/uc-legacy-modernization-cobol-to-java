package com.cognition.usersecurity.controller;

import com.cognition.usersecurity.dto.*;
import com.cognition.usersecurity.exception.ApiException;
import com.cognition.usersecurity.model.User;
import com.cognition.usersecurity.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserRepository repository;
    public UserController(UserRepository repository) { this.repository = repository; }

    @PostMapping("/auth/signon")
    public SignonResponse signon(@Valid @RequestBody SignonRequest request) {
        User user = repository.findByIdIgnoreCase(request.userId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User ID NOT found..."));
        if (!user.getPassword().equals(request.password().toUpperCase())) throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid password...");
        return new SignonResponse(user.getId(), user.getUserType(), "Signon successful");
    }
    @GetMapping("/users")
    public Page<UserResponse> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        if (page < 0 || size < 1) throw new ApiException(HttpStatus.BAD_REQUEST, "Page and size must be positive");
        return repository.findAll(PageRequest.of(page, size)).map(UserResponse::from);
    }
    @GetMapping("/users/{id}")
    public UserResponse get(@PathVariable String id) { return UserResponse.from(find(id)); }
    @PostMapping("/users")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        if (request.userType() == null) throw new ApiException(HttpStatus.BAD_REQUEST, "User Type can NOT be empty...");
        if (repository.existsById(request.userId().toUpperCase())) throw new ApiException(HttpStatus.CONFLICT, "User ID already exists...");
        User user = new User(request.userId().toUpperCase(), request.firstName(), request.lastName(), request.password(), request.userType());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(repository.save(user)));
    }
    @PutMapping("/users/{id}")
    public UserResponse update(@PathVariable String id, @Valid @RequestBody UserUpdateRequest request) {
        if (request.userType() == null) throw new ApiException(HttpStatus.BAD_REQUEST, "User Type can NOT be empty...");
        User user = find(id); user.update(request.firstName(), request.lastName(), request.password(), request.userType());
        return UserResponse.from(repository.save(user));
    }
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) { repository.delete(find(id)); return ResponseEntity.noContent().build(); }
    private User find(String id) { return repository.findByIdIgnoreCase(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User ID NOT found...")); }
}
