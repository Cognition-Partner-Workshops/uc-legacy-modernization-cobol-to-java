/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.controller;

import com.cardemo.model.TransactionRecord;
import com.cardemo.service.online.TransactionListService;
import com.cardemo.service.online.TransactionViewService;
import com.cardemo.service.online.TransactionAddService;
import com.cardemo.service.online.BillPaymentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Transaction Controller - REST API for transaction operations.
 * Migrated from COBOL CICS programs COTRN00C.cbl, COTRN01C.cbl,
 * COTRN02C.cbl, COBIL00C.cbl.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionListService transactionListService;
    private final TransactionViewService transactionViewService;
    private final TransactionAddService transactionAddService;
    private final BillPaymentService billPaymentService;

    public TransactionController(TransactionListService transactionListService,
                                 TransactionViewService transactionViewService,
                                 TransactionAddService transactionAddService,
                                 BillPaymentService billPaymentService) {
        this.transactionListService = transactionListService;
        this.transactionViewService = transactionViewService;
        this.transactionAddService = transactionAddService;
        this.billPaymentService = billPaymentService;
    }

    @GetMapping
    public ResponseEntity<Page<TransactionRecord>> listTransactions(
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(transactionListService.listTransactions(page));
    }

    @GetMapping("/{tranId}")
    public ResponseEntity<TransactionRecord> viewTransaction(@PathVariable String tranId) {
        TransactionRecord transaction = transactionViewService.viewTransaction(tranId);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transaction);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addTransaction(
            @RequestParam String cardNum,
            @RequestParam String tranTypeCd,
            @RequestParam int tranCatCd,
            @RequestParam String tranSource,
            @RequestParam String tranDesc,
            @RequestParam BigDecimal tranAmt,
            @RequestParam long merchantId,
            @RequestParam String merchantName,
            @RequestParam(defaultValue = "") String merchantCity,
            @RequestParam(defaultValue = "") String merchantZip) {
        try {
            TransactionRecord saved = transactionAddService.addTransaction(
                    cardNum, tranTypeCd, tranCatCd, tranSource, tranDesc,
                    tranAmt, merchantId, merchantName, merchantCity, merchantZip);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Transaction added successfully",
                    "transaction", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }

    @PostMapping("/bill-payment")
    public ResponseEntity<Map<String, Object>> processBillPayment(
            @RequestParam long acctId,
            @RequestParam BigDecimal paymentAmount,
            @RequestParam String cardNum) {
        try {
            billPaymentService.processBillPayment(acctId, paymentAmount, cardNum);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Bill payment processed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }
}
