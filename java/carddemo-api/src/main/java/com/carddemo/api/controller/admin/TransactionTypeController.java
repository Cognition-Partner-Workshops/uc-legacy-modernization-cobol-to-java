package com.carddemo.api.controller.admin;

import com.carddemo.common.model.TransactionType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Transaction Type controller replacing COBOL programs:
 * - COTRTLIC — Transaction Type List
 * - COTRTUPC — Transaction Type Update (optional DB2 module)
 *
 * Original COBOL: app/cbl/COTRTLIC.cbl (if present), app/cbl/COTRTUPC.cbl (if present)
 */
@RestController
@RequestMapping("/api/admin/transaction-types")
public class TransactionTypeController {

    /**
     * GET /api/admin/transaction-types — List all transaction types.
     * Replaces COTRTLIC.
     */
    @GetMapping
    public ResponseEntity<List<TransactionType>> listTransactionTypes() {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COTRTLIC");
    }

    /**
     * GET /api/admin/transaction-types/{typeCode} — Get transaction type by code.
     */
    @GetMapping("/{typeCode}")
    public ResponseEntity<TransactionType> getTransactionType(
            @PathVariable String typeCode) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COTRTLIC");
    }

    /**
     * POST /api/admin/transaction-types — Create a new transaction type.
     */
    @PostMapping
    public ResponseEntity<TransactionType> createTransactionType(
            @RequestBody TransactionType transactionType) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COTRTUPC");
    }

    /**
     * PUT /api/admin/transaction-types/{typeCode} — Update a transaction type.
     * Replaces COTRTUPC.
     */
    @PutMapping("/{typeCode}")
    public ResponseEntity<TransactionType> updateTransactionType(
            @PathVariable String typeCode,
            @RequestBody TransactionType transactionType) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COTRTUPC");
    }

    /**
     * DELETE /api/admin/transaction-types/{typeCode} — Delete a transaction type.
     */
    @DeleteMapping("/{typeCode}")
    public ResponseEntity<Void> deleteTransactionType(@PathVariable String typeCode) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COTRTUPC");
    }
}
