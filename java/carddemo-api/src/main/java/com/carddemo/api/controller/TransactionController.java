package com.carddemo.api.controller;

import com.carddemo.common.dto.TransactionDto;
import com.carddemo.api.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Transaction controller replacing COBOL programs:
 * - COTRN00C (CT00 transaction) — Transaction List (paginated, replaces PF7/PF8 logic)
 * - COTRN01C (CT01 transaction) — Transaction Detail View
 * - COTRN02C (CT02 transaction) — Transaction Add
 *
 * The paginated list endpoint replaces the PF7/PF8 page-forward/backward logic
 * in COTRN00C that used STARTBR/READNEXT/READPREV CICS commands.
 *
 * Original COBOL: app/cbl/COTRN00C.cbl, app/cbl/COTRN01C.cbl, app/cbl/COTRN02C.cbl
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * GET /api/transactions — Paginated transaction list.
     * Replaces COTRN00C (CT00) with PF7/PF8 paging via Spring Data Pageable.
     */
    @GetMapping
    public ResponseEntity<Page<TransactionDto>> listTransactions(Pageable pageable) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COTRN00C");
    }

    /**
     * GET /api/transactions/{id} — View transaction details.
     * Replaces COTRN01C (CT01 transaction).
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable String id) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COTRN01C");
    }

    /**
     * POST /api/transactions — Create a new transaction.
     * Replaces COTRN02C (CT02 transaction).
     * Includes validation: xref lookup, account lookup, overlimit check, expiration check.
     */
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(
            @RequestBody TransactionDto transactionDto) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COTRN02C");
    }
}
