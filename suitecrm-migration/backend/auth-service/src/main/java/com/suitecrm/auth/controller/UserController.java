package com.suitecrm.auth.controller;

import com.suitecrm.auth.dto.UserDto;
import com.suitecrm.auth.entity.User;
import com.suitecrm.auth.entity.Role;
import com.suitecrm.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Page<UserDto>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateEntered") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.searchUsers(search, pageable);
        } else {
            users = userRepository.findByDeletedFalse(pageable);
        }

        Page<UserDto> dtoPage = users.map(this::mapToDto);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @securityService.isOwner(#id)")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(mapToDto(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeleted(true);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneWork(user.getPhoneWork())
                .phoneMobile(user.getPhoneMobile())
                .title(user.getTitle())
                .department(user.getDepartment())
                .status(user.getStatus())
                .isAdmin(user.getIsAdmin())
                .timezone(user.getTimezone())
                .language(user.getLanguage())
                .dateEntered(user.getDateEntered())
                .dateModified(user.getDateModified())
                .lastLogin(user.getLastLogin())
                .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
