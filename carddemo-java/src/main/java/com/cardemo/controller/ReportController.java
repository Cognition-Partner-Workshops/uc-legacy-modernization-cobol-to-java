package com.cardemo.controller;

import com.cardemo.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Report Controller - converted from COBOL programs CORPT00C.cbl and CBTRN03C.cbl
 * Original: CICS Transaction Reports screen (CORPT00C) and
 * Batch Transaction Report generator (CBTRN03C)
 * Replaces BMS map CORPT00 and batch JCL job with REST endpoint.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * GET /api/reports/daily-transactions
     * Generate a daily transaction report - replaces CORPT00C / CBTRN03C.
     */
    @GetMapping("/daily-transactions")
    public ResponseEntity<ReportService.TransactionReport> getDailyTransactionReport() {
        return ResponseEntity.ok(reportService.generateDailyReport());
    }
}
