package com.carddemo.api.controller;

import com.carddemo.api.service.BillPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bill Payment controller replacing COBOL program COBIL00C (CB00 transaction).
 * Handles bill payment processing.
 *
 * Original COBOL: app/cbl/COBIL00C.cbl
 * CICS Transaction: CB00
 */
@RestController
@RequestMapping("/api/bill-payments")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    /**
     * POST /api/bill-payments — Process a bill payment.
     * Replaces COBIL00C (CB00 transaction).
     */
    @PostMapping
    public ResponseEntity<Void> processBillPayment() {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COBIL00C");
    }
}
