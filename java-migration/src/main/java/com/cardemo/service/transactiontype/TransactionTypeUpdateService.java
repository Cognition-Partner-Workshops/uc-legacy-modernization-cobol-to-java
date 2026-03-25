package com.cardemo.service.transactiontype;

import com.cardemo.model.TransactionTypeRecord;
import com.cardemo.repository.TransactionTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transaction Type Update Service - migrated from COBOL program COTRTUPC.cbl.
 * Handles add/update operations for transaction type records.
 * Original: CICS COBOL DB2 program for adding and updating
 *           transaction type records in DB2.
 */
@Service
public class TransactionTypeUpdateService {

    private static final Logger log = LoggerFactory.getLogger(TransactionTypeUpdateService.class);

    private final TransactionTypeRepository transactionTypeRepository;

    public TransactionTypeUpdateService(TransactionTypeRepository transactionTypeRepository) {
        this.transactionTypeRepository = transactionTypeRepository;
    }

    /**
     * Add a new transaction type.
     * Migrated from ADD-TRANSACTION-TYPE paragraph.
     *
     * @param record the transaction type record to add
     * @return the saved record
     */
    @Transactional
    public TransactionTypeRecord addTransactionType(TransactionTypeRecord record) {
        if (transactionTypeRepository.existsById(record.getTranType())) {
            throw new TransactionTypeException("Transaction type already exists: " + record.getTranType());
        }
        TransactionTypeRecord saved = transactionTypeRepository.save(record);
        log.info("Transaction type {} added successfully", saved.getTranType());
        return saved;
    }

    /**
     * Update an existing transaction type.
     * Migrated from UPDATE-TRANSACTION-TYPE paragraph.
     *
     * @param record the transaction type record to update
     * @return the updated record
     */
    @Transactional
    public TransactionTypeRecord updateTransactionType(TransactionTypeRecord record) {
        if (!transactionTypeRepository.existsById(record.getTranType())) {
            throw new TransactionTypeException("Transaction type not found: " + record.getTranType());
        }
        TransactionTypeRecord saved = transactionTypeRepository.save(record);
        log.info("Transaction type {} updated successfully", saved.getTranType());
        return saved;
    }

    /**
     * Delete a transaction type.
     * Migrated from DELETE-TRANSACTION-TYPE paragraph.
     *
     * @param typeCode the transaction type code to delete
     */
    @Transactional
    public void deleteTransactionType(String typeCode) {
        if (!transactionTypeRepository.existsById(typeCode)) {
            throw new TransactionTypeException("Transaction type not found: " + typeCode);
        }
        transactionTypeRepository.deleteById(typeCode);
        log.info("Transaction type {} deleted successfully", typeCode);
    }

    public static class TransactionTypeException extends RuntimeException {
        public TransactionTypeException(String message) {
            super(message);
        }
    }
}
