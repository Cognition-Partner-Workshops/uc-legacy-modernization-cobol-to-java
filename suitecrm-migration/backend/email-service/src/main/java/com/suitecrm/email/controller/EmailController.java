package com.suitecrm.email.controller;

import com.suitecrm.email.dto.*;
import com.suitecrm.email.service.EmailManagementService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailManagementService emailService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<Page<EmailDto>> listEmails(Pageable pageable) {
        return ResponseEntity.ok(emailService.listEmails(pageable));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<EmailDto>> listMyEmails(
            @AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(emailService.listEmailsByUser(userId, pageable));
    }

    @GetMapping("/mailbox/{mailboxId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<EmailDto>> listByMailbox(
            @PathVariable UUID mailboxId, Pageable pageable) {
        return ResponseEntity.ok(emailService.listEmailsByMailbox(mailboxId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmailDto> getEmail(@PathVariable UUID id) {
        return ResponseEntity.ok(emailService.getEmail(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmailDto> createEmail(
            @Valid @RequestBody EmailCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(emailService.createEmail(request, userId));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmailDto> sendEmail(@PathVariable UUID id) {
        return ResponseEntity.ok(emailService.sendEmail(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteEmail(@PathVariable UUID id) {
        emailService.deleteEmail(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<EmailDto>> searchEmails(
            @RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(emailService.searchEmails(query, pageable));
    }

    @GetMapping("/flagged")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EmailDto>> getFlaggedEmails(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(emailService.getFlaggedEmails(userId));
    }

    @PutMapping("/{id}/flag")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmailDto> toggleFlag(@PathVariable UUID id) {
        return ResponseEntity.ok(emailService.toggleFlag(id));
    }

    @GetMapping("/inbound-accounts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<InboundEmailDto>> listInboundAccounts() {
        return ResponseEntity.ok(emailService.listInboundAccounts());
    }

    @GetMapping("/inbound-accounts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InboundEmailDto> getInboundAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(emailService.getInboundAccount(id));
    }

    @GetMapping("/outbound-accounts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<OutboundEmailDto>> listOutboundAccounts() {
        return ResponseEntity.ok(emailService.listOutboundAccounts());
    }

    @GetMapping("/outbound-accounts/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OutboundEmailDto>> getMyOutboundAccounts(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(emailService.getUserOutboundAccounts(userId));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Long>> getEmailStats() {
        return ResponseEntity.ok(Map.of(
                "draft", emailService.countByStatus("draft"),
                "sent", emailService.countByStatus("sent"),
                "read", emailService.countByStatus("read"),
                "unread", emailService.countByStatus("unread"),
                "send_error", emailService.countByStatus("send_error")
        ));
    }
}
