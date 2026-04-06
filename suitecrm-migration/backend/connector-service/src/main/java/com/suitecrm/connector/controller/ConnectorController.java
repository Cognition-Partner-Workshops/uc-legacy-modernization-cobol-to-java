package com.suitecrm.connector.controller;

import com.suitecrm.connector.dto.*;
import com.suitecrm.connector.entity.*;
import com.suitecrm.connector.service.ConnectorService;
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
@RequestMapping("/api/v1/connectors")
@RequiredArgsConstructor
public class ConnectorController {

    private final ConnectorService connectorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<ConnectorDto>> listConnectors(Pageable pageable) {
        return ResponseEntity.ok(connectorService.listConnectors(pageable));
    }

    @GetMapping("/enabled")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConnectorDto>> listEnabled() {
        return ResponseEntity.ok(connectorService.listEnabledConnectors());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ConnectorDto> getConnector(@PathVariable UUID id) {
        return ResponseEntity.ok(connectorService.getConnector(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConnectorDto> createConnector(
            @Valid @RequestBody ConnectorCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(connectorService.createConnector(request, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConnectorDto> updateConnector(
            @PathVariable UUID id, @Valid @RequestBody ConnectorCreateRequest request) {
        return ResponseEntity.ok(connectorService.updateConnector(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteConnector(@PathVariable UUID id) {
        connectorService.deleteConnector(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConnectorDto> toggleConnector(@PathVariable UUID id) {
        return ResponseEntity.ok(connectorService.toggleConnector(id));
    }

    // External accounts
    @GetMapping("/{connectorId}/accounts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ExternalAccountDto>> getExternalAccounts(@PathVariable UUID connectorId) {
        return ResponseEntity.ok(connectorService.getExternalAccounts(connectorId));
    }

    @GetMapping("/accounts/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ExternalAccountDto>> getMyAccounts(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(connectorService.getUserExternalAccounts(userId));
    }

    // OAuth keys
    @GetMapping("/oauth-keys")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OAuthKeyDto>> listOAuthKeys() {
        return ResponseEntity.ok(connectorService.listOAuthKeys());
    }

    // Logs
    @GetMapping("/{connectorId}/logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<ConnectorLog>> getConnectorLogs(@PathVariable UUID connectorId, Pageable pageable) {
        return ResponseEntity.ok(connectorService.getConnectorLogs(connectorId, pageable));
    }

    // Mappings
    @GetMapping("/{connectorId}/mappings")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<IntegrationMapping>> getMappings(@PathVariable UUID connectorId) {
        return ResponseEntity.ok(connectorService.getMappings(connectorId));
    }
}
