package com.cardemo.service.messaging;

import com.cardemo.model.AccountRecord;
import com.cardemo.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Account Message Service - migrated from COBOL program COACCT01.cbl (app-vsam-mq).
 * Processes account inquiry/update requests received via messaging.
 * Original: CICS COBOL program that reads account requests from MQ,
 *           performs VSAM CRUD operations, and sends responses back via MQ.
 *
 * Functions supported:
 * - INQY: Account inquiry - read and return account details
 * - UPDT: Account update - update account record
 * - ADD:  Account add - create new account record
 * - DELT: Account delete - remove account record
 */
@Service
public class AccountMessageService {

    private static final Logger log = LoggerFactory.getLogger(AccountMessageService.class);

    private final AccountRepository accountRepository;

    public AccountMessageService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Process an account inquiry request.
     * Migrated from 4100-INQUIRY-ACCT paragraph.
     *
     * @param acctId the account ID to inquire about
     * @return the account record or null if not found
     */
    public AccountRecord inquireAccount(long acctId) {
        log.info("COACCT01: Account inquiry for ID: {}", acctId);
        Optional<AccountRecord> acctOpt = accountRepository.findById(acctId);
        if (acctOpt.isEmpty()) {
            log.warn("COACCT01: Account {} not found", acctId);
            return null;
        }
        log.info("COACCT01: Account {} found successfully", acctId);
        return acctOpt.get();
    }

    /**
     * Process an account update request.
     * Migrated from 4200-UPDATE-ACCT paragraph.
     *
     * @param account the account record with updated data
     * @return the updated account record
     */
    @Transactional
    public AccountRecord updateAccount(AccountRecord account) {
        log.info("COACCT01: Account update for ID: {}", account.getAcctId());
        if (!accountRepository.existsById(account.getAcctId())) {
            log.warn("COACCT01: Account {} not found for update", account.getAcctId());
            throw new AccountMessageException("Account not found: " + account.getAcctId());
        }
        AccountRecord saved = accountRepository.save(account);
        log.info("COACCT01: Account {} updated successfully", saved.getAcctId());
        return saved;
    }

    /**
     * Process an account add request.
     * Migrated from 4300-ADD-ACCT paragraph.
     *
     * @param account the account record to add
     * @return the saved account record
     */
    @Transactional
    public AccountRecord addAccount(AccountRecord account) {
        log.info("COACCT01: Account add for ID: {}", account.getAcctId());
        if (accountRepository.existsById(account.getAcctId())) {
            log.warn("COACCT01: Account {} already exists", account.getAcctId());
            throw new AccountMessageException("Account already exists: " + account.getAcctId());
        }
        AccountRecord saved = accountRepository.save(account);
        log.info("COACCT01: Account {} added successfully", saved.getAcctId());
        return saved;
    }

    /**
     * Process an account delete request.
     * Migrated from 4400-DELETE-ACCT paragraph.
     *
     * @param acctId the account ID to delete
     */
    @Transactional
    public void deleteAccount(long acctId) {
        log.info("COACCT01: Account delete for ID: {}", acctId);
        if (!accountRepository.existsById(acctId)) {
            log.warn("COACCT01: Account {} not found for delete", acctId);
            throw new AccountMessageException("Account not found: " + acctId);
        }
        accountRepository.deleteById(acctId);
        log.info("COACCT01: Account {} deleted successfully", acctId);
    }

    public static class AccountMessageException extends RuntimeException {
        public AccountMessageException(String message) {
            super(message);
        }
    }
}
