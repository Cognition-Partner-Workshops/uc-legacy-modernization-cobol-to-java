package com.carddemo.controller;

import com.carddemo.dto.BillPaymentRequest;
import com.carddemo.dto.TransactionRequest;
import com.carddemo.model.Transaction;
import com.carddemo.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Transaction controller - replaces COBOL programs:
 *   COTRN00C.cbl (CT00 transaction)  -> GET  /api/transactions
 *   COTRN01C.cbl (CT01 transaction)  -> GET  /api/transactions/{id}
 *   COTRN02C.cbl (CT02 transaction)  -> POST /api/transactions
 *   COBIL00C.cbl (CB00 transaction)  -> POST /api/transactions/bill-payment
 *   CORPT00C.cbl (CR00 transaction)  -> GET  /api/transactions/report
 *
 * Legacy CICS flows:
 *   COTRN00C: STARTBR/READNEXT/READPREV on TRANSACT VSAM file with pagination
 *   COTRN02C: WRITE to TRANSACT file after input validation
 *   COBIL00C: Read ACCTDAT, validate, update balance, write transaction
 *
 * Modernized: Reactive REST endpoints with Cassandra (transactions)
 *             and JDBC (transaction type validation)
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public Flux<Transaction> listTransactions(
            @RequestParam(required = false) String cardNumber) {
        if (cardNumber != null && !cardNumber.isBlank()) {
            return transactionService.getTransactionsByCardNumber(cardNumber);
        }
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{transactionId}")
    public Mono<Transaction> viewTransaction(@PathVariable String transactionId) {
        return transactionService.getTransactionById(transactionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Transaction> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        return transactionService.createTransaction(request);
    }

    @PostMapping("/bill-payment")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Transaction> processBillPayment(
            @Valid @RequestBody BillPaymentRequest request) {
        return transactionService.processBillPayment(request);
    }

    @GetMapping("/report")
    public Flux<Transaction> generateReport(
            @RequestParam(required = false) String typeCode) {
        return transactionService.generateReport(typeCode);
    }
}
