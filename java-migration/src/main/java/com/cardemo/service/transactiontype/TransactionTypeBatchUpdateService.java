/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.transactiontype;

import com.cardemo.model.TransactionTypeRecord;
import com.cardemo.repository.TransactionTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Transaction Type Batch Update Service - migrated from COBOL program COBTUPDT.cbl.
 * Handles batch update operations for transaction type records.
 * Original: Batch COBOL DB2 program for bulk updating transaction type records.
 */
@Service
public class TransactionTypeBatchUpdateService {

    private static final Logger log = LoggerFactory.getLogger(TransactionTypeBatchUpdateService.class);

    private final TransactionTypeRepository transactionTypeRepository;

    public TransactionTypeBatchUpdateService(TransactionTypeRepository transactionTypeRepository) {
        this.transactionTypeRepository = transactionTypeRepository;
    }

    /**
     * Batch update transaction type records.
     * Migrated from main PROCEDURE DIVISION logic.
     *
     * @param records list of records to update
     * @return number of records updated
     */
    @Transactional
    public int batchUpdate(List<TransactionTypeRecord> records) {
        log.info("START OF EXECUTION OF COBTUPDT (TransactionTypeBatchUpdate)");

        int count = 0;
        for (TransactionTypeRecord record : records) {
            if (transactionTypeRepository.existsById(record.getTranType())) {
                transactionTypeRepository.save(record);
                count++;
                log.info("Updated transaction type: {}", record.getTranType());
            } else {
                log.warn("Transaction type not found for update: {}", record.getTranType());
            }
        }

        log.info("END OF EXECUTION OF COBTUPDT. Records updated: {}", count);
        return count;
    }
}
