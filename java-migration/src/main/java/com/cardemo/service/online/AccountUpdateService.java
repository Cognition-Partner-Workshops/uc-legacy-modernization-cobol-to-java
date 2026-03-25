/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.online;

import com.cardemo.model.AccountRecord;
import com.cardemo.model.CardXrefRecord;
import com.cardemo.model.CustomerRecord;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Account Update Service - migrated from COBOL program COACTUPC.cbl.
 * Handles updating account details.
 * Original: CICS program with TRANID CAUP, reads/rewrites ACCTDAT.
 */
@Service
public class AccountUpdateService {

    private static final Logger log = LoggerFactory.getLogger(AccountUpdateService.class);

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;

    public AccountUpdateService(AccountRepository accountRepository,
                                CardXrefRepository cardXrefRepository,
                                CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Get account for update - equivalent to READ with UPDATE in CICS.
     */
    public AccountRecord getAccountForUpdate(long acctId) {
        Optional<AccountRecord> acctOpt = accountRepository.findById(acctId);
        if (acctOpt.isEmpty()) {
            throw new AccountUpdateException("Account ID NOT found...");
        }
        return acctOpt.get();
    }

    /**
     * Get customer info for account display.
     */
    public CustomerRecord getCustomerForAccount(long acctId) {
        List<CardXrefRecord> xrefs = cardXrefRepository.findByXrefAcctId(acctId);
        if (xrefs.isEmpty()) {
            throw new AccountUpdateException("Card cross-reference not found for account...");
        }
        Optional<CustomerRecord> custOpt = customerRepository.findById(xrefs.get(0).getXrefCustId());
        if (custOpt.isEmpty()) {
            throw new AccountUpdateException("Customer not found...");
        }
        return custOpt.get();
    }

    /**
     * Update account - migrated from REWRITE ACCTDAT logic.
     */
    @Transactional
    public AccountRecord updateAccount(AccountRecord updatedAccount) {
        if (!accountRepository.existsById(updatedAccount.getAcctId())) {
            throw new AccountUpdateException("Account ID NOT found...");
        }
        AccountRecord saved = accountRepository.save(updatedAccount);
        log.info("Account {} updated successfully", saved.getAcctId());
        return saved;
    }

    public static class AccountUpdateException extends RuntimeException {
        public AccountUpdateException(String message) {
            super(message);
        }
    }
}
