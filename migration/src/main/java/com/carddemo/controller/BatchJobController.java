package com.carddemo.controller;

import com.carddemo.service.InterestCalculationService;
import com.carddemo.service.TransactionPostingService;
import com.carddemo.service.DataMigrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * REST endpoints to trigger batch jobs.
 * Admin-only access (configured in SecurityConfig).
 */
@RestController
@RequestMapping("/api/batch")
public class BatchJobController {

    private final TransactionPostingService transactionPostingService;
    private final InterestCalculationService interestCalculationService;
    private final DataMigrationService dataMigrationService;

    public BatchJobController(TransactionPostingService transactionPostingService,
                               InterestCalculationService interestCalculationService,
                               DataMigrationService dataMigrationService) {
        this.transactionPostingService = transactionPostingService;
        this.interestCalculationService = interestCalculationService;
        this.dataMigrationService = dataMigrationService;
    }

    @PostMapping("/interest-calculation")
    public ResponseEntity<Map<String, Object>> runInterestCalculation() {
        InterestCalculationService.InterestResult result =
                interestCalculationService.calculateInterest(LocalDate.now());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "completed");
        response.put("recordsProcessed", result.recordsProcessed());
        response.put("transactionsCreated", result.transactionsCreated());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/data-export")
    public ResponseEntity<Map<String, Object>> runDataExport() {
        Map<String, Object> data = dataMigrationService.exportAll();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ready");
        status.put("message", "Batch job endpoints are available");
        return ResponseEntity.ok(status);
    }
}
