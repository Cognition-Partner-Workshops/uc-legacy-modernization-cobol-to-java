package com.carddemo.transaction.service;

import com.carddemo.transaction.model.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Low-level helper that persists a Transaction inside its own dedicated
 * transaction (REQUIRES_NEW). This is a separate bean so that the Spring
 * CGLIB proxy can intercept the call — self-invocation within the same bean
 * would bypass the proxy and the REQUIRES_NEW annotation would be ignored.
 *
 * Mirrors the step1-cics-monolith TransactionIdPersister pattern.
 */
@Service
public class TransactionIdPersister {

    private final TransactionRepository transactionRepository;

    public TransactionIdPersister(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Compute the next transaction ID and save the transaction in a new,
     * independent transaction that commits immediately.
     *
     * @param transaction the Transaction entity (all fields set except tranId)
     * @return the saved Transaction with its generated tranId
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction computeAndSave(Transaction transaction) {
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
