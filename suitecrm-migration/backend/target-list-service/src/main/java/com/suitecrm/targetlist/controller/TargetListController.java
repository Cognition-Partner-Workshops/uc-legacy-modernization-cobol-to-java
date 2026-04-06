package com.suitecrm.targetlist.controller;

import com.suitecrm.targetlist.dto.*;
import com.suitecrm.targetlist.service.TargetListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/target-lists")
@RequiredArgsConstructor
public class TargetListController {

    private final TargetListService targetListService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TargetListDto>> listTargetLists(Pageable pageable) {
        return ResponseEntity.ok(targetListService.listTargetLists(pageable));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TargetListDto>> listByType(@PathVariable String type, Pageable pageable) {
        return ResponseEntity.ok(targetListService.listByType(type, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TargetListDto> getTargetList(@PathVariable UUID id) {
        return ResponseEntity.ok(targetListService.getTargetList(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<TargetListDto> createTargetList(
            @Valid @RequestBody TargetListCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(targetListService.createTargetList(request, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<TargetListDto> updateTargetList(
            @PathVariable UUID id, @Valid @RequestBody TargetListCreateRequest request) {
        return ResponseEntity.ok(targetListService.updateTargetList(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteTargetList(@PathVariable UUID id) {
        targetListService.deleteTargetList(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TargetListDto>> search(@RequestParam String q, Pageable pageable) {
        return ResponseEntity.ok(targetListService.searchTargetLists(q, pageable));
    }

    // Members
    @GetMapping("/{listId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TargetListMemberDto>> getMembers(@PathVariable UUID listId) {
        return ResponseEntity.ok(targetListService.getMembers(listId));
    }

    @PostMapping("/{listId}/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<TargetListMemberDto> addMember(
            @PathVariable UUID listId, @RequestBody TargetListMemberDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(targetListService.addMember(listId, request));
    }

    @DeleteMapping("/members/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> removeMember(@PathVariable UUID memberId) {
        targetListService.removeMember(memberId);
        return ResponseEntity.noContent().build();
    }

    // Targets
    @GetMapping("/targets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TargetDto>> listTargets(Pageable pageable) {
        return ResponseEntity.ok(targetListService.listTargets(pageable));
    }

    @GetMapping("/targets/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TargetDto>> searchTargets(@RequestParam String q, Pageable pageable) {
        return ResponseEntity.ok(targetListService.searchTargets(q, pageable));
    }
}
