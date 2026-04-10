package com.carddemo.transaction.service;

import com.carddemo.transaction.model.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Centralized service for generating unique transaction IDs and persisting
 * transactions atomically. The generate-and-save must happen inside the same
 * synchronized block so that no other thread can read the same MAX(tran_id)
 * before the new row is flushed to the database.
 *
 * Mirrors the step1-cics-monolith TransactionIdService pattern.
 *
 * Note: synchronized only serializes within a single JVM. For multi-instance
 * deployments, use a database sequence or SELECT ... FOR UPDATE instead.
 */
@Service
public class TransactionIdService {

    private final TransactionRepository transactionRepository;

    public TransactionIdService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Generate the next transaction ID, assign it to the given Transaction,
     * and save it — all within a single synchronized block.
     * This prevents the race condition where Thread A generates an ID, releases
     * the lock, and Thread B generates the same ID before Thread A's save()
     * has committed.
     *
     * @param transaction the Transaction entity (all fields set except tranId)
     * @return the saved Transaction with its generated tranId
     */
    public synchronized Transaction generateIdAndSave(Transaction transaction) {
        String nextId = computeNextId();
        transaction.setTranId(nextId);
        return transactionRepository.save(transaction);
    }

    private String computeNextId() {
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
