package com.suitecrm.auth.controller;

import com.suitecrm.auth.entity.Role;
import com.suitecrm.auth.entity.Permission;
import com.suitecrm.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listRoles() {
        List<Role> roles = roleRepository.findAll().stream()
                .filter(r -> !r.getDeleted())
                .toList();

        List<Map<String, Object>> response = roles.stream()
                .map(role -> Map.<String, Object>of(
                        "id", role.getId(),
                        "name", role.getName(),
                        "description", role.getDescription() != null ? role.getDescription() : "",
                        "permissions", role.getPermissions().stream()
                                .map(p -> p.getModuleName() + ":" + p.getActionName())
                                .collect(Collectors.toSet())
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRole(@PathVariable UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Map<String, Object> response = Map.of(
                "id", role.getId(),
                "name", role.getName(),
                "description", role.getDescription() != null ? role.getDescription() : "",
                "permissions", role.getPermissions().stream()
                        .map(p -> Map.of(
                                "id", p.getId(),
                                "module", p.getModuleName(),
                                "action", p.getActionName(),
                                "accessLevel", p.getAccessLevel()
                        ))
                        .collect(Collectors.toSet())
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        if (roleRepository.existsByName(role.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Role savedRole = roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRole);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        role.setDeleted(true);
        roleRepository.save(role);
        return ResponseEntity.noContent().build();
    }
}
