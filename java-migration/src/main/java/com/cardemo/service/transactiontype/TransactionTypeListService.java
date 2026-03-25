/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.transactiontype;

import com.cardemo.model.TransactionTypeRecord;
import com.cardemo.repository.TransactionTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Transaction Type List Service - migrated from COBOL program COTRTLIC.cbl.
 * Lists transaction types for updates and deletes with pagination.
 * Original: CICS COBOL DB2 program demonstrating paging with cursors
 *           and simple select, delete and update use cases.
 */
@Service
public class TransactionTypeListService {

    private static final Logger log = LoggerFactory.getLogger(TransactionTypeListService.class);
    private static final int PAGE_SIZE = 7;

    private final TransactionTypeRepository transactionTypeRepository;

    public TransactionTypeListService(TransactionTypeRepository transactionTypeRepository) {
        this.transactionTypeRepository = transactionTypeRepository;
    }

    /**
     * List all transaction types with pagination.
     * Migrated from cursor-based DB2 paging logic.
     *
     * @param page the page number (0-based)
     * @return page of transaction type records
     */
    public Page<TransactionTypeRecord> listTransactionTypes(int page) {
        return transactionTypeRepository.findAll(PageRequest.of(page, PAGE_SIZE));
    }

    /**
     * List transaction types filtered by type code.
     *
     * @param typeCode the type code filter
     * @return matching transaction type records
     */
    public List<TransactionTypeRecord> findByTypeCode(String typeCode) {
        return transactionTypeRepository.findAll().stream()
                .filter(tt -> tt.getTranType() != null && tt.getTranType().contains(typeCode))
                .toList();
    }

    /**
     * Get a specific transaction type.
     *
     * @param typeCode the transaction type code
     * @return the transaction type record or null
     */
    public TransactionTypeRecord getTransactionType(String typeCode) {
        return transactionTypeRepository.findById(typeCode).orElse(null);
    }
}
