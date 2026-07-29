package com.carddemo.usersecurity.web;

import com.carddemo.usersecurity.domain.User;
import com.carddemo.usersecurity.dto.CreateUserRequest;
import com.carddemo.usersecurity.dto.PageResponse;
import com.carddemo.usersecurity.dto.UpdateUserRequest;
import com.carddemo.usersecurity.dto.UserResponse;
import com.carddemo.usersecurity.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
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
import org.springframework.web.util.UriComponentsBuilder;

/** User maintenance endpoints, modernizing COUSR00C through COUSR03C. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public PageResponse<UserResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> users = userService.list(page, size);
        List<UserResponse> content = users.getContent().stream().map(UserResponse::from).toList();
        return PageResponse.of(users, content);
    }

    @GetMapping("/{userId}")
    public UserResponse get(@PathVariable String userId) {
        return UserResponse.from(userService.getById(userId));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User created = userService.create(new User(request.getUserId(), request.getFirstName(),
                request.getLastName(), request.getPassword(), request.getUserType()));
        return ResponseEntity
                .created(UriComponentsBuilder.fromPath("/api/users/{userId}").build(created.getUserId()))
                .body(UserResponse.from(created));
    }

    @PutMapping("/{userId}")
    public UserResponse update(@PathVariable String userId, @Valid @RequestBody UpdateUserRequest request) {
        return UserResponse.from(userService.update(userId, request.getFirstName(), request.getLastName(),
                request.getPassword(), request.getUserType()));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable String userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
