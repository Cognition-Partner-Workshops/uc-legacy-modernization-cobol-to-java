package com.carddemo.transaction.controller;

import com.carddemo.transaction.model.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public List<Transaction> listTransactions() {
        return transactionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String id) {
        return transactionRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/card/{cardNum}")
    public List<Transaction> getTransactionsByCard(@PathVariable String cardNum) {
        return transactionRepository.findByTranCardNumOrderByTranIdDesc(cardNum);
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        // Auto-generate transaction ID if not provided
        if (transaction.getTranId() == null || transaction.getTranId().isBlank()) {
            String nextId = transactionRepository.findMaxTranId()
                .map(maxId -> {
                    try {
                        long id = Long.parseLong(maxId.trim());
                        return String.format("%016d", id + 1);
                    } catch (NumberFormatException e) {
                        return String.format("%016d", System.currentTimeMillis());
                    }
                })
                .orElse("0000000000000001");
            transaction.setTranId(nextId);
        }
        return ResponseEntity.ok(transactionRepository.save(transaction));
    }
}
