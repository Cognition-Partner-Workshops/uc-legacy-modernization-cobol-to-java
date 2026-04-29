package com.cardemo.web.service;

import com.cardemo.common.repository.TransactionRepository;
import org.springframework.stereotype.Service;

/**
 * Transaction service replacing logic from COTRN00C.cbl, COTRN01C.cbl, COTRN02C.cbl.
 *
 * TODO: Implement transaction list with search/filter from COTRN00C.cbl
 * TODO: Implement transaction view from COTRN01C.cbl
 * TODO: Implement transaction add with validation from COTRN02C.cbl
 * TODO: All amounts must use BigDecimal (mapped from PIC S9(09)V99)
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // TODO: Implement transaction operations from COTRN00C/01C/02C
}
