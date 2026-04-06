package com.suitecrm.calendar.controller;

import com.suitecrm.calendar.dto.*;
import com.suitecrm.calendar.service.CalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/calls")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP','ROLE_SUPPORT_REP')")
    public ResponseEntity<Page<CallDto>> getAllCalls(Pageable pageable) {
        return ResponseEntity.ok(calendarService.getAllCalls(pageable));
    }

    @GetMapping("/calls/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP','ROLE_SUPPORT_REP')")
    public ResponseEntity<CallDto> getCallById(@PathVariable UUID id) {
        return ResponseEntity.ok(calendarService.getCallById(id));
    }

    @PostMapping("/calls")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP','ROLE_SUPPORT_REP')")
    public ResponseEntity<CallDto> createCall(@Valid @RequestBody CallCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarService.createCall(request));
    }

    @GetMapping("/meetings")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP','ROLE_SUPPORT_REP')")
    public ResponseEntity<Page<MeetingDto>> getAllMeetings(Pageable pageable) {
        return ResponseEntity.ok(calendarService.getAllMeetings(pageable));
    }

    @GetMapping("/meetings/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP','ROLE_SUPPORT_REP')")
    public ResponseEntity<MeetingDto> getMeetingById(@PathVariable UUID id) {
        return ResponseEntity.ok(calendarService.getMeetingById(id));
    }

    @PostMapping("/meetings")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP','ROLE_SUPPORT_REP')")
    public ResponseEntity<MeetingDto> createMeeting(@Valid @RequestBody MeetingCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarService.createMeeting(request));
    }

    @GetMapping("/calendar")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP','ROLE_SUPPORT_REP')")
    public ResponseEntity<List<CallDto>> getCallsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(calendarService.getCallsByDateRange(start, end));
    }
}
