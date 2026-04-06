package com.suitecrm.notification.controller;

import com.suitecrm.notification.dto.*;
import com.suitecrm.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/notifications") @RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/alerts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AlertDto>> getAlerts(@RequestParam UUID userId, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getAlertsByUser(userId, pageable));
    }
    @GetMapping("/alerts/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AlertDto>> getUnreadAlerts(@RequestParam UUID userId) {
        return ResponseEntity.ok(notificationService.getUnreadAlerts(userId));
    }
    @PutMapping("/alerts/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAlertRead(@PathVariable UUID id) {
        notificationService.markAlertRead(id); return ResponseEntity.ok().build();
    }
    @GetMapping("/favorites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FavoriteDto>> getFavorites(@RequestParam UUID userId) {
        return ResponseEntity.ok(notificationService.getFavorites(userId));
    }
    @PostMapping("/favorites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FavoriteDto> addFavorite(@RequestParam UUID userId, @RequestParam String moduleName, @RequestParam UUID recordId) {
        return ResponseEntity.ok(notificationService.addFavorite(userId, moduleName, recordId));
    }
}
