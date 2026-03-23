package com.carddemo.service;

import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.entity.Account;
import com.carddemo.exception.AccountNotFoundException;
import com.carddemo.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Replaces COACTVWC.cbl (account view) and COACTUPC.cbl (account update).
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account getAccount(String acctId) {
        return accountRepository.findById(acctId)
                .orElseThrow(() -> new AccountNotFoundException(acctId));
    }

    public Page<Account> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    @Transactional
    public Account updateAccount(String acctId, AccountUpdateRequest request) {
        Account account = getAccount(acctId);

        if (request.getActiveStatus() != null) {
            account.setActiveStatus(request.getActiveStatus());
        }
        if (request.getCreditLimit() != null) {
            account.setCreditLimit(request.getCreditLimit());
        }
        if (request.getCashCreditLimit() != null) {
            account.setCashCreditLimit(request.getCashCreditLimit());
        }
        if (request.getExpirationDate() != null) {
            account.setExpirationDate(request.getExpirationDate());
        }
        if (request.getReissueDate() != null) {
            account.setReissueDate(request.getReissueDate());
        }
        if (request.getZipCode() != null) {
            account.setZipCode(request.getZipCode());
        }
        if (request.getGroupId() != null) {
            account.setGroupId(request.getGroupId());
        }

        return accountRepository.save(account);
    }
}
