package com.carddemo.controller;

import com.carddemo.dto.TransactionDto;
import com.carddemo.service.online.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Report controller replacing COBOL program CORPT00C (Transaction Reports, CICS txn CR00).
 *
 * <p>CORPT00C allows querying transactions by various criteria:
 * - By account/card number
 * - By date range
 * - By transaction type/category
 *
 * <p>Full reporting logic will be implemented in Phase 2.
 * This stub provides the endpoint structure matching the CICS transaction mapping.
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Transaction and account reporting (replaces CORPT00C / txn CR00)")
public class ReportController {

    private final TransactionService transactionService;

    public ReportController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions")
    @Operation(summary = "Transaction report",
            description = "Generate transaction report with optional filters. "
                    + "Replaces CORPT00C (CICS txn CR00). "
                    + "Full filtering (date range, type, category) to be implemented in Phase 2.")
    public ResponseEntity<Page<TransactionDto>> transactionReport(
            @Parameter(description = "Optional card number filter")
            @RequestParam(required = false) String cardNum,
            @Parameter(description = "Optional transaction type filter")
            @RequestParam(required = false) String tranTypeCd,
            Pageable pageable) {
        // Phase 1: basic listing; Phase 2: full report with date range, type, category filters
        if (cardNum != null) {
            return ResponseEntity.ok(transactionService.listTransactionsByCard(cardNum, pageable));
        }
        return ResponseEntity.ok(transactionService.listTransactions(pageable));
    }
}
