/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.controller;

import com.cardemo.model.TransactionTypeRecord;
import com.cardemo.service.transactiontype.TransactionTypeListService;
import com.cardemo.service.transactiontype.TransactionTypeUpdateService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Transaction Type Controller - REST API for transaction type management.
 * Migrated from COBOL CICS programs COTRTLIC.cbl and COTRTUPC.cbl.
 */
@RestController
@RequestMapping("/api/transaction-types")
public class TransactionTypeController {

    private final TransactionTypeListService listService;
    private final TransactionTypeUpdateService updateService;

    public TransactionTypeController(TransactionTypeListService listService,
                                     TransactionTypeUpdateService updateService) {
        this.listService = listService;
        this.updateService = updateService;
    }

    @GetMapping
    public ResponseEntity<Page<TransactionTypeRecord>> listTransactionTypes(
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(listService.listTransactionTypes(page));
    }

    @GetMapping("/{typeCode}")
    public ResponseEntity<TransactionTypeRecord> getTransactionType(@PathVariable String typeCode) {
        TransactionTypeRecord type = listService.getTransactionType(typeCode);
        if (type == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(type);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addTransactionType(
            @RequestBody TransactionTypeRecord record) {
        try {
            TransactionTypeRecord saved = updateService.addTransactionType(record);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Transaction type added successfully",
                    "transactionType", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }

    @PutMapping("/{typeCode}")
    public ResponseEntity<Map<String, Object>> updateTransactionType(
            @PathVariable String typeCode,
            @RequestBody TransactionTypeRecord record) {
        try {
            record.setTranType(typeCode);
            TransactionTypeRecord updated = updateService.updateTransactionType(record);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Transaction type updated successfully",
                    "transactionType", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{typeCode}")
    public ResponseEntity<Map<String, Object>> deleteTransactionType(@PathVariable String typeCode) {
        try {
            updateService.deleteTransactionType(typeCode);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Transaction type deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }
}
