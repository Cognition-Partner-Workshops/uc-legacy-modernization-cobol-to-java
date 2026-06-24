package com.cardemo.repository;

import com.cardemo.model.Transaction;
import java.util.Optional;

/**
 * Repository interface for transaction data access.
 * Equivalent of COBOL TRANSACT VSAM file operations.
 */
public interface TransactionRepository {

    void save(Transaction transaction);

    Optional<Transaction> findLastTransaction();
}
