package com.cardemo.controller;

import com.cardemo.model.Transaction;
import com.cardemo.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Transaction Controller - converted from COBOL programs COTRN00C, COTRN01C, COTRN02C
 * Original: CICS Transaction List, View, and Add screens
 * Replaces BMS maps COTRN00/COTRN01/COTRN02 with REST endpoints.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * GET /api/transactions
     * List all transactions.
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> listAllTransactions() {
        return ResponseEntity.ok(transactionService.listAllTransactions());
    }

    /**
     * GET /api/transactions/by-card?cardNum={cardNum}&page={page}&size={size}
     * List transactions for a card with pagination - replaces COTRN00C.
     * Supports PF7/PF8 page forward/backward via page parameter.
     */
    @GetMapping("/by-card")
    public ResponseEntity<Page<Transaction>> listByCard(
            @RequestParam String cardNum,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(transactionService.listTransactions(cardNum, pageable));
    }

    /**
     * GET /api/transactions/{tranId}
     * View transaction details - replaces COTRN01C SEND-TRNVIEW-SCREEN.
     */
    @GetMapping("/{tranId}")
    public ResponseEntity<Transaction> viewTransaction(@PathVariable String tranId) {
        return transactionService.viewTransaction(tranId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/transactions
     * Add a new transaction - replaces COTRN02C PROCESS-ENTER-KEY.
     * Includes validation from CBTRN02C batch program.
     */
    @PostMapping
    public ResponseEntity<Object> addTransaction(@RequestBody Transaction transaction) {
        try {
            Transaction saved = transactionService.addTransaction(transaction);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }
}
