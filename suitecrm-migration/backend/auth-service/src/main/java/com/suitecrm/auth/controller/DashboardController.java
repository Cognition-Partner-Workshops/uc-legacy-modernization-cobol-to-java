package com.suitecrm.auth.controller;

import com.suitecrm.auth.entity.Dashboard;
import com.suitecrm.auth.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Dashboard>> getUserDashboards(@PathVariable UUID userId) {
        return ResponseEntity.ok(dashboardService.getUserDashboards(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dashboard> getDashboard(@PathVariable UUID id) {
        return ResponseEntity.ok(dashboardService.getDashboard(id));
    }

    @PostMapping
    public ResponseEntity<Dashboard> createDashboard(@RequestBody Dashboard dashboard) {
        return ResponseEntity.ok(dashboardService.createDashboard(dashboard));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dashboard> updateDashboard(@PathVariable UUID id, @RequestBody Dashboard dashboard) {
        return ResponseEntity.ok(dashboardService.updateDashboard(id, dashboard));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDashboard(@PathVariable UUID id) {
        dashboardService.deleteDashboard(id);
        return ResponseEntity.noContent().build();
    }
}
