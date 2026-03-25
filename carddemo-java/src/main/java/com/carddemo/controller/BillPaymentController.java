package com.carddemo.controller;

import com.carddemo.dto.BillPaymentRequest;
import com.carddemo.entity.Transaction;
import com.carddemo.service.BillPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bill payment controller - replaces COBIL00C (573 lines).
 * Original COBOL: account lookup, card resolution, transaction ID generation,
 * payment transaction creation, account balance update.
 */
@RestController
@RequestMapping("/api/payments")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    @PostMapping
    public ResponseEntity<Transaction> makePayment(@Valid @RequestBody BillPaymentRequest request) {
        Transaction payment = billPaymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}
