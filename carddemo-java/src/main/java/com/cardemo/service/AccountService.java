package com.cardemo.service;

import com.cardemo.model.Account;
import com.cardemo.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Account Service - converted from COBOL programs COACTVWC.cbl and COACTUPC.cbl
 * Original: CICS Account View and Account Update screens
 * Handles account viewing and updating operations.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * View account details - equivalent to COACTVWC (Account View).
     */
    public Optional<Account> viewAccount(Long acctId) {
        return accountRepository.findById(acctId);
    }

    /**
     * List all accounts.
     */
    public List<Account> listAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Update account details - equivalent to COACTUPC (Account Update).
     */
    @Transactional
    public Optional<Account> updateAccount(Long acctId, Account updatedData) {
        return accountRepository.findById(acctId).map(existing -> {
            if (updatedData.getActiveStatus() != null) {
                existing.setActiveStatus(updatedData.getActiveStatus());
            }
            if (updatedData.getCreditLimit() != null) {
                existing.setCreditLimit(updatedData.getCreditLimit());
            }
            if (updatedData.getCashCreditLimit() != null) {
                existing.setCashCreditLimit(updatedData.getCashCreditLimit());
            }
            if (updatedData.getExpirationDate() != null) {
                existing.setExpirationDate(updatedData.getExpirationDate());
            }
            if (updatedData.getReissueDate() != null) {
                existing.setReissueDate(updatedData.getReissueDate());
            }
            if (updatedData.getAddressZip() != null) {
                existing.setAddressZip(updatedData.getAddressZip());
            }
            if (updatedData.getGroupId() != null) {
                existing.setGroupId(updatedData.getGroupId());
            }
            return accountRepository.save(existing);
        });
    }

    /**
     * Update account balance - used by batch processing (CBACT04C interest calculation).
     */
    @Transactional
    public void updateBalance(Long acctId, BigDecimal interestAmount) {
        accountRepository.findById(acctId).ifPresent(account -> {
            BigDecimal currentBal = account.getCurrentBalance() != null
                    ? account.getCurrentBalance() : BigDecimal.ZERO;
            account.setCurrentBalance(currentBal.add(interestAmount));
            account.setCurrentCycleCredit(BigDecimal.ZERO);
            account.setCurrentCycleDebit(BigDecimal.ZERO);
            accountRepository.save(account);
        });
    }
}
