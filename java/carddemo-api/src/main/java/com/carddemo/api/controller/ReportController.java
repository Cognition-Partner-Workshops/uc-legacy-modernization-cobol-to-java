package com.carddemo.api.controller;

import com.carddemo.api.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Report controller replacing COBOL program CORPT00C (CR00 transaction).
 * Handles generation of transaction reports.
 *
 * Original COBOL: app/cbl/CORPT00C.cbl
 * CICS Transaction: CR00
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * POST /api/reports/transactions — Generate transaction report.
     * Replaces CORPT00C (CR00 transaction).
     */
    @PostMapping("/transactions")
    public ResponseEntity<Void> generateTransactionReport() {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program CORPT00C");
    }
}
