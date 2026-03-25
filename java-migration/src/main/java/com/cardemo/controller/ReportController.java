/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.controller;

import com.cardemo.service.online.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Report Controller - REST API for report generation.
 * Migrated from COBOL CICS program CORPT00C.cbl.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> generateTransactionReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            Map<String, Object> report = reportService.generateTransactionReport(startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }
}
