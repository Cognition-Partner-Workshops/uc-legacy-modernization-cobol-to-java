/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.online;

import com.cardemo.model.TransactionRecord;
import com.cardemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Transaction View Service - migrated from COBOL program COTRN01C.cbl.
 * Handles viewing transaction details.
 * Original: CICS program with TRANID CT01, reads TRANSACT file.
 */
@Service
public class TransactionViewService {

    private static final Logger log = LoggerFactory.getLogger(TransactionViewService.class);

    private final TransactionRepository transactionRepository;

    public TransactionViewService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * View a transaction by ID.
     * Migrated from PROCESS-ENTER-KEY and READ-TRANSACT-FILE paragraphs.
     */
    public TransactionRecord viewTransaction(String tranId) {
        if (tranId == null || tranId.isBlank()) {
            throw new TransactionViewException("Transaction ID not provided...");
        }

        Optional<TransactionRecord> tranOpt = transactionRepository.findById(tranId.trim());
        if (tranOpt.isEmpty()) {
            throw new TransactionViewException("Transaction NOT found...");
        }

        log.info("Transaction {} viewed successfully", tranId);
        return tranOpt.get();
    }

    public static class TransactionViewException extends RuntimeException {
        public TransactionViewException(String message) {
            super(message);
        }
    }
}
