package com.suitecrm.auth.controller;

import com.suitecrm.auth.dto.AclActionDto;
import com.suitecrm.auth.dto.AclRoleDto;
import com.suitecrm.auth.dto.SecurityGroupDto;
import com.suitecrm.auth.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class SecurityGroupController {

    private final UserManagementService userManagementService;

    // ==================== Security Groups ====================

    @GetMapping("/security-groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SecurityGroupDto>> listSecurityGroups(Pageable pageable) {
        return ResponseEntity.ok(userManagementService.listSecurityGroups(pageable));
    }

    @GetMapping("/security-groups/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SecurityGroupDto> getSecurityGroup(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.getSecurityGroup(id));
    }

    @PostMapping("/security-groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SecurityGroupDto> createSecurityGroup(@RequestBody SecurityGroupDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userManagementService.createSecurityGroup(request));
    }

    // ==================== ACL Roles ====================

    @GetMapping("/acl-roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AclRoleDto>> listAclRoles(Pageable pageable) {
        return ResponseEntity.ok(userManagementService.listAclRoles(pageable));
    }

    @GetMapping("/acl-roles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AclRoleDto> getAclRole(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.getAclRole(id));
    }

    @PostMapping("/acl-roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AclRoleDto> createAclRole(@RequestBody AclRoleDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userManagementService.createAclRole(request));
    }

    // ==================== ACL Actions ====================

    @GetMapping("/acl-actions/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getActionCategories() {
        return ResponseEntity.ok(userManagementService.getActionCategories());
    }

    @GetMapping("/acl-actions/category/{category}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AclActionDto>> getActionsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(userManagementService.getActionsByCategory(category));
    }
}
