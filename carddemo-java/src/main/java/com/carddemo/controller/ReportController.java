package com.carddemo.controller;

import com.carddemo.dto.ReportRequest;
import com.carddemo.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * Report controller - replaces CORPT00C (650 lines).
 * Original COBOL: accepts report type and date range, constructs JCL,
 * submits to INTRDR via TDQ for batch execution.
 * Java: triggers report generation directly or via Spring Batch.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<Map<String, Object>> submitTransactionReport(
            @Valid @RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.submitReport(request));
    }
}
