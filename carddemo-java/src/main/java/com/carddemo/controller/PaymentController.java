package com.carddemo.controller;

import com.carddemo.dto.BillPaymentRequest;
import com.carddemo.dto.TransactionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Payment controller replacing COBOL program COBIL00C (Bill Payment, CICS txn CB00).
 *
 * <p>COBIL00C processes bill payments by:
 * 1. Validating account and card via XREFFILE
 * 2. Creating a payment transaction
 * 3. Updating account balance
 *
 * <p>Full payment logic will be implemented in Phase 2.
 * This stub provides the endpoint structure matching the CICS transaction mapping.
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Bill payment processing (replaces COBIL00C / txn CB00)")
public class PaymentController {

    @PostMapping
    @Operation(summary = "Process bill payment",
            description = "Submit a bill payment. Replaces COBIL00C (CICS txn CB00). "
                    + "Validates card/account cross-reference, creates payment transaction, "
                    + "and updates account balance. Full implementation in Phase 2.")
    public ResponseEntity<TransactionDto> processPayment(
            @Valid @RequestBody BillPaymentRequest request) {
        // Phase 2: implement full payment processing logic
        // - Validate card via XREFFILE
        // - Check account balance
        // - Create payment transaction
        // - Update account balance (debit)
        throw new UnsupportedOperationException(
                "Bill payment processing will be fully implemented in Phase 2");
    }
}
