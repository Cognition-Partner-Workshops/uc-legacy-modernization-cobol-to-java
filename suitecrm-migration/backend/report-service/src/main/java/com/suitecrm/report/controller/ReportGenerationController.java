package com.suitecrm.report.controller;

import com.suitecrm.report.engine.ReportEngine;
import com.suitecrm.report.engine.ReportResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportGenerationController {

    private final ReportEngine reportEngine;

    @GetMapping("/{reportId}/generate")
    public ResponseEntity<ReportResult> generateReport(@PathVariable UUID reportId) {
        ReportResult result = reportEngine.generateReport(reportId);
        return ResponseEntity.ok(result);
    }
}
