package com.suitecrm.scheduler.controller;

import com.suitecrm.scheduler.dto.*;
import com.suitecrm.scheduler.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/schedulers") @RequiredArgsConstructor
public class SchedulerController {
    private final SchedulerService schedulerService;

    @GetMapping @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<SchedulerDto>> getAllSchedulers(Pageable pageable) { return ResponseEntity.ok(schedulerService.getAllSchedulers(pageable)); }

    @GetMapping("/{id}") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<SchedulerDto> getSchedulerById(@PathVariable UUID id) { return ResponseEntity.ok(schedulerService.getSchedulerById(id)); }

    @GetMapping("/jobs") @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<SchedulerJobDto>> getAllJobs(Pageable pageable) { return ResponseEntity.ok(schedulerService.getAllJobs(pageable)); }
}
