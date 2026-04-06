package com.suitecrm.event.controller;

import com.suitecrm.event.dto.*;
import com.suitecrm.event.service.EventService;
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
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<FPEventDto>> listEvents(Pageable pageable) {
        return ResponseEntity.ok(eventService.listEvents(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FPEventDto> getEvent(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<FPEventDto> createEvent(
            @Valid @RequestBody FPEventCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<FPEventDto> updateEvent(
            @PathVariable UUID id, @Valid @RequestBody FPEventCreateRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/upcoming")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FPEventDto>> getUpcomingEvents() {
        return ResponseEntity.ok(eventService.getUpcomingEvents());
    }

    @GetMapping("/{eventId}/registrations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventRegistrationDto>> getRegistrations(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventService.getEventRegistrations(eventId));
    }

    @PostMapping("/{eventId}/registrations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventRegistrationDto> register(
            @PathVariable UUID eventId, @RequestBody EventRegistrationDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.registerForEvent(eventId, request));
    }

    @GetMapping("/locations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FPEventLocationDto>> listLocations() {
        return ResponseEntity.ok(eventService.listLocations());
    }

    @PostMapping("/locations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<FPEventLocationDto> createLocation(
            @RequestBody FPEventLocationDto request, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createLocation(request, userId));
    }
}
