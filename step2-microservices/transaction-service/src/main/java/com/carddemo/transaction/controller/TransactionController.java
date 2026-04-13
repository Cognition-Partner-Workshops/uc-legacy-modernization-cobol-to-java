package com.carddemo.transaction.controller;

import com.carddemo.transaction.model.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import com.carddemo.transaction.service.TransactionIdService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final TransactionIdService transactionIdService;

    public TransactionController(TransactionRepository transactionRepository,
                                 TransactionIdService transactionIdService) {
        this.transactionRepository = transactionRepository;
        this.transactionIdService = transactionIdService;
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
        // If an explicit ID is provided, save directly
        if (transaction.getTranId() != null && !transaction.getTranId().isBlank()) {
            return ResponseEntity.ok(transactionRepository.save(transaction));
        }
        // Otherwise, generate ID and save atomically via synchronized service
        return ResponseEntity.ok(transactionIdService.generateIdAndSave(transaction));
    }
}
