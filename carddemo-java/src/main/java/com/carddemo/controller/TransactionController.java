package com.carddemo.controller;

import com.carddemo.dto.TransactionAddRequest;
import com.carddemo.dto.TransactionDto;
import com.carddemo.service.online.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Transaction controller replacing COBOL programs COTRN00C, COTRN01C, COTRN02C.
 *
 * <p>COTRN00C (Transaction List, CICS txn CT00) -> GET /api/transactions
 * <p>COTRN01C (Transaction Detail, CICS txn CT01) -> GET /api/transactions/{id}
 * <p>COTRN02C (Transaction Add, CICS txn CT02) -> POST /api/transactions
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Transaction list, view, and add (replaces COTRN00C/01C/02C)")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "List transactions",
            description = "Browse transactions with pagination. Replaces COTRN00C (CICS txn CT00). "
                    + "Optionally filter by card number. Original uses STARTBR/READNEXT on TRANSACT.")
    public ResponseEntity<Page<TransactionDto>> listTransactions(
            @Parameter(description = "Optional card number filter")
            @RequestParam(required = false) String cardNum,
            Pageable pageable) {
        if (cardNum != null) {
            return ResponseEntity.ok(transactionService.listTransactionsByCard(cardNum, pageable));
        }
        return ResponseEntity.ok(transactionService.listTransactions(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "View transaction details",
            description = "Retrieve transaction by ID. Replaces COTRN01C (CICS txn CT01). "
                    + "Original reads TRANSACT by RIDFLD(TRAN-ID).")
    public ResponseEntity<TransactionDto> getTransaction(
            @Parameter(description = "Transaction ID (TRAN-ID, PIC X(16))")
            @PathVariable String id) {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }

    @PostMapping
    @Operation(summary = "Add new transaction",
            description = "Create a daily transaction for batch processing. Replaces COTRN02C (CICS txn CT02). "
                    + "Validates card via XREFFILE, writes to DALYTRAN. "
                    + "Transaction is posted to TRANSACT by the PostTransaction batch job.")
    public ResponseEntity<TransactionDto> addTransaction(
            @Valid @RequestBody TransactionAddRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.addTransaction(request));
    }
}
