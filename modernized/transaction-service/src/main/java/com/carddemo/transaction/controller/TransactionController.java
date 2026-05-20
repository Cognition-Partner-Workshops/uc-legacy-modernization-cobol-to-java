package com.carddemo.transaction.controller;

import com.carddemo.common.dto.PageResponse;
import com.carddemo.transaction.dto.*;
import com.carddemo.transaction.service.ReportService;
import com.carddemo.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final ReportService reportService;

    public TransactionController(TransactionService transactionService, ReportService reportService) {
        this.transactionService = transactionService;
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<TransactionDto>> listTransactions(
            @RequestParam String cardNum,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.listTransactions(cardNum, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }

    @PostMapping
    public ResponseEntity<TransactionDto> addTransaction(@Valid @RequestBody AddTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.addTransaction(request));
    }

    @PostMapping("/report")
    public ResponseEntity<List<TransactionDto>> generateReport(@RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.generateTransactionReport(request));
    }
}
