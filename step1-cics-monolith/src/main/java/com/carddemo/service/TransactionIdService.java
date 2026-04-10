package com.carddemo.service;

import com.carddemo.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Centralized service for generating unique transaction IDs.
 * Consolidates the duplicated generateNextTranId() logic from
 * TransactionAddController, BillPaymentController, InterestCalculationJob,
 * and TransactionPostingJob into a single synchronized method to prevent
 * race conditions that could produce duplicate primary keys.
 */
@Service
public class TransactionIdService {

    private final TransactionRepository transactionRepository;

    public TransactionIdService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Generate the next transaction ID atomically.
     * Uses synchronized to prevent concurrent threads from reading the same
     * max ID and generating duplicates. The method reads the current max
     * tran_id, increments by 1, and returns the zero-padded 16-digit string.
     */
    @Transactional
    public synchronized String generateNextTranId() {
        Optional<String> maxId = transactionRepository.findMaxTranId();
        if (maxId.isPresent()) {
            try {
                long id = Long.parseLong(maxId.get().trim());
                return String.format("%016d", id + 1);
            } catch (NumberFormatException e) {
                return String.format("%016d", System.currentTimeMillis());
            }
        }
        return "0000000000000001";
    }
}
