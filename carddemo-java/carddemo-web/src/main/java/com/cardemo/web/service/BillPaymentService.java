package com.cardemo.web.service;

import com.cardemo.common.repository.AccountRepository;
import com.cardemo.common.repository.TransactionRepository;
import org.springframework.stereotype.Service;

/**
 * Bill payment service replacing logic from COBIL00C.cbl.
 *
 * TODO: Implement bill payment processing from COBIL00C.cbl
 * TODO: Update account balance (ACCT-CURR-BAL) after payment
 * TODO: Create transaction record for payment
 * TODO: All amounts must use BigDecimal
 */
@Service
public class BillPaymentService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BillPaymentService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // TODO: Implement bill payment from COBIL00C.cbl
}
