package com.carddemo.controller.api;

import com.carddemo.dto.TransactionAddRequest;
import com.carddemo.model.Transaction;
import com.carddemo.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionApiController {

    private final TransactionService transactionService;

    public TransactionApiController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<Page<Transaction>> listTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String cardNum) {
        Page<Transaction> transactions;
        if (cardNum != null && !cardNum.isBlank()) {
            transactions = transactionService.getTransactionsByCardNum(cardNum, page);
        } else {
            transactions = transactionService.getTransactions(page);
        }
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{tranId}")
    public ResponseEntity<?> getTransaction(@PathVariable String tranId) {
        return transactionService.findById(tranId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> addTransaction(@Valid @RequestBody TransactionAddRequest request) {
        Transaction transaction = transactionService.addTransaction(request);
        return ResponseEntity.ok(transaction);
    }
}
