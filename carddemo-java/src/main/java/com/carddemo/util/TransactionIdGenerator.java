package com.carddemo.util;

import com.carddemo.repository.TransactionRepository;
import org.springframework.stereotype.Component;

/**
 * Generates unique transaction IDs - replaces COBOL's STARTBR/READPREV pattern
 * used in COBIL00C and COTRN02C to find the last transaction ID and increment.
 */
@Component
public class TransactionIdGenerator {

    private final TransactionRepository transactionRepository;

    public TransactionIdGenerator(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public synchronized String generateNextId() {
        String maxId = transactionRepository.findMaxTransactionId().orElse("0000000000000000");
        long nextNum = Long.parseLong(maxId.trim()) + 1;
        return String.format("%016d", nextNum);
    }
}
