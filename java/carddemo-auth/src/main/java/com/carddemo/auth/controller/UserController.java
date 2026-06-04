package com.carddemo.auth.controller;

import com.carddemo.auth.dto.UserDto;
import com.carddemo.auth.model.User;
import com.carddemo.auth.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * User-maintenance REST API - the modern replacement for the {@code USRSEC}
 * management programs that the legacy admin menu ({@code COADM01C}) dispatched
 * to. All endpoints require the {@code ADMIN} role (enforced in
 * {@code SecurityConfig}), mirroring the admin-only legacy menu.
 *
 * <p>Mapping to legacy programs:
 * <ul>
 *   <li>{@code GET  /api/users}      &rarr; {@code COUSR00C} (sequential browse / paged list).</li>
 *   <li>{@code POST /api/users}      &rarr; {@code COUSR01C} (write new {@code USRSEC} record).</li>
 *   <li>{@code GET  /api/users/{id}} &rarr; read-by-key (as used by {@code COUSR02C}/{@code COUSR03C}).</li>
 *   <li>{@code PUT  /api/users/{id}} &rarr; {@code COUSR02C} (read + rewrite).</li>
 *   <li>{@code DELETE /api/users/{id}} &rarr; {@code COUSR03C} (delete record).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Paginated user list. Replaces the {@code COUSR00C} sequential browse
     * ({@code STARTBR}/{@code READNEXT}) that paged through {@code USRSEC}.
     */
    @GetMapping
    public Page<UserDto> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDto::fromEntity);
    }

    /**
     * Fetch a single user by ID. Replaces the keyed {@code READ} of
     * {@code USRSEC} performed by {@code COUSR02C}/{@code COUSR03C}.
     *
     * @throws ResponseStatusException {@code 404} if no such user
     */
    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable("id") String id) {
        return userRepository.findById(id.toUpperCase())
                .map(UserDto::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User ID NOT found..."));
    }

    /**
     * Create a user. Replaces {@code COUSR01C} ({@code WRITE-USER-SEC-FILE}),
     * including its empty-field edits (enforced via bean validation) and its
     * duplicate-key handling ({@code DFHRESP(DUPKEY)}/{@code DUPREC}).
     *
     * @throws ResponseStatusException {@code 409} if the user ID already exists
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto dto,
                                              UriComponentsBuilder uriBuilder) {
        String userId = dto.userId().toUpperCase();
        if (userRepository.existsById(userId)) {
            // Legacy: "User ID already exist..."
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "User ID already exist...");
        }

        User user = new User(
                userId,
                dto.firstName(),
                dto.lastName(),
                passwordEncoder.encode(dto.password()),
                dto.userType());
        User saved = userRepository.save(user);

        URI location = uriBuilder.path("/api/users/{id}")
                .buildAndExpand(saved.getUserId()).toUri();
        return ResponseEntity.created(location).body(UserDto.fromEntity(saved));
    }

    /**
     * Update a user. Replaces {@code COUSR02C} (read existing record, apply
     * changes, {@code REWRITE}). A blank password leaves the stored hash
     * unchanged; a supplied password is re-hashed.
     *
     * @throws ResponseStatusException {@code 404} if no such user
     */
    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable("id") String id, @RequestBody UserDto dto) {
        User user = userRepository.findById(id.toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User ID NOT found..."));

        if (dto.firstName() != null && !dto.firstName().isBlank()) {
            user.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null && !dto.lastName().isBlank()) {
            user.setLastName(dto.lastName());
        }
        if (dto.userType() != null) {
            user.setUserType(dto.userType());
        }
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        return UserDto.fromEntity(userRepository.save(user));
    }

    /**
     * Delete a user. Replaces {@code COUSR03C} ({@code DELETE} from
     * {@code USRSEC}).
     *
     * @throws ResponseStatusException {@code 404} if no such user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") String id) {
        String userId = id.toUpperCase();
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User ID NOT found...");
        }
        userRepository.deleteById(userId);
        return ResponseEntity.noContent().build();
    }
}
