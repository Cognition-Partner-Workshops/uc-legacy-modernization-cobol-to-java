package com.cognition.usersecurity.controller;

import com.cognition.usersecurity.dto.*;
import com.cognition.usersecurity.exception.ApiException;
import com.cognition.usersecurity.model.User;
import com.cognition.usersecurity.repository.UserRepository;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Sign on a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Signon successful"),
        @ApiResponse(responseCode = "400", description = "Validation failure",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid password",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public SignonResponse signon(@Valid @RequestBody SignonRequest request) {
        User user = find(request.userId());
        if (!user.getPassword().equals(request.password())) throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid password...");
        return new SignonResponse(user.getId(), user.getUserType(), user.getUserType().getCode(), "Signon successful");
    }
    @GetMapping("/users")
    @Operation(summary = "List users")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paginated users"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public Page<UserResponse> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        if (page < 0 || size < 1) throw new ApiException(HttpStatus.BAD_REQUEST, "Page and size must be positive");
        return repository.findAll(PageRequest.of(page, size)).map(UserResponse::from);
    }
    @GetMapping("/users/{id}")
    @Operation(summary = "Get a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public UserResponse get(@PathVariable String id) { return UserResponse.from(find(id)); }
    @PostMapping("/users")
    @Operation(summary = "Create a user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created"),
        @ApiResponse(responseCode = "400", description = "Validation failure",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "User ID already exists",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        if (request.userType() == null) throw new ApiException(HttpStatus.BAD_REQUEST, "User Type can NOT be empty...");
        String userId = normalizeId(request.userId());
        if (repository.existsById(userId)) throw new ApiException(HttpStatus.CONFLICT, "User ID already exists...");
        User user = new User(userId, request.firstName(), request.lastName(), request.password(), request.userType());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(repository.save(user)));
    }
    @PutMapping("/users/{id}")
    @Operation(summary = "Update a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated"),
        @ApiResponse(responseCode = "400", description = "Validation failure",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public UserResponse update(@PathVariable String id, @Valid @RequestBody UserUpdateRequest request) {
        if (request.userType() == null) throw new ApiException(HttpStatus.BAD_REQUEST, "User Type can NOT be empty...");
        User user = find(id); user.update(request.firstName(), request.lastName(), request.password(), request.userType());
        return UserResponse.from(repository.save(user));
    }
    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User deleted"),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable String id) { repository.delete(find(id)); return ResponseEntity.noContent().build(); }
    private User find(String id) {
        return repository.findById(normalizeId(id)).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User ID NOT found..."));
    }
    private String normalizeId(String id) { return id.toUpperCase(); }
}
