package com.cardemo.controller;

import com.cardemo.model.Transaction;
import com.cardemo.service.BillPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Bill Payment Controller - converted from COBOL program COBIL00C.cbl
 * Original: CICS Bill Payment screen
 * Replaces BMS map COBIL00 with REST endpoint.
 */
@RestController
@RequestMapping("/api/payments")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    /**
     * POST /api/payments
     * Process a bill payment - replaces COBIL00C PROCESS-ENTER-KEY.
     */
    @PostMapping
    public ResponseEntity<Object> processPayment(@RequestBody Map<String, Object> request) {
        try {
            Long acctId = Long.valueOf(request.get("acctId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());

            Transaction payment = billPaymentService.processPayment(acctId, amount);
            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Missing required fields: acctId, amount"));
        }
    }
}
