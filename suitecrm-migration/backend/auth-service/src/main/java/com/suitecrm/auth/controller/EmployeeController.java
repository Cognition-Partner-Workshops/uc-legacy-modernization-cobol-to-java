package com.suitecrm.auth.controller;

import com.suitecrm.auth.dto.EmployeeDto;
import com.suitecrm.auth.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserManagementService userManagementService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Page<EmployeeDto>> listEmployees(Pageable pageable) {
        return ResponseEntity.ok(userManagementService.listEmployees(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.getEmployee(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Page<EmployeeDto>> searchEmployees(@RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(userManagementService.searchEmployees(query, pageable));
    }

    @GetMapping("/{managerId}/direct-reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<EmployeeDto>> getDirectReports(@PathVariable UUID managerId) {
        return ResponseEntity.ok(userManagementService.getDirectReports(managerId));
    }

    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<String>> getDepartments() {
        return ResponseEntity.ok(userManagementService.getDepartments());
    }
}
