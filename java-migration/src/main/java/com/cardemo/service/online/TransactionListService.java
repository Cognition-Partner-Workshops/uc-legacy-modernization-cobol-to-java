/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.online;

import com.cardemo.model.TransactionRecord;
import com.cardemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * Transaction List Service - migrated from COBOL program COTRN00C.cbl.
 * Handles listing transactions with pagination (PF7/PF8 page forward/backward).
 * Original: CICS program with TRANID CT00, browses TRANSACT file.
 */
@Service
public class TransactionListService {

    private static final Logger log = LoggerFactory.getLogger(TransactionListService.class);
    private static final int PAGE_SIZE = 10;

    private final TransactionRepository transactionRepository;

    public TransactionListService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * List transactions page forward - migrated from PROCESS-PAGE-FORWARD.
     * Replaces CICS STARTBR/READNEXT/ENDBR on TRANSACT.
     */
    public Page<TransactionRecord> listTransactionsForward(String fromTranId, int pageNum) {
        String startId = (fromTranId == null || fromTranId.isBlank()) ? "" : fromTranId;
        return transactionRepository.findByTranIdGreaterThanEqualOrderByTranIdAsc(
                startId, PageRequest.of(pageNum, PAGE_SIZE));
    }

    /**
     * List transactions page backward - migrated from PROCESS-PAGE-BACKWARD.
     * Replaces CICS STARTBR/READPREV/ENDBR on TRANSACT.
     */
    public Page<TransactionRecord> listTransactionsBackward(String fromTranId, int pageNum) {
        String startId = (fromTranId == null || fromTranId.isBlank()) ? "9999999999999999" : fromTranId;
        return transactionRepository.findByTranIdLessThanEqualOrderByTranIdDesc(
                startId, PageRequest.of(pageNum, PAGE_SIZE));
    }

    /**
     * Get all transactions with pagination.
     */
    public Page<TransactionRecord> listTransactions(int page) {
        return transactionRepository.findAll(PageRequest.of(page, PAGE_SIZE));
    }
}
