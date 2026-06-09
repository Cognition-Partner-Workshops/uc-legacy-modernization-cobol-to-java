package com.carddemo.service;

import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.Account;
import com.carddemo.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Page<Account> findAll(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Account findById(long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));
    }

    @Transactional
    public Account update(long id, Account updated) {
        Account existing = findById(id);
        existing.setActiveStatus(updated.getActiveStatus());
        existing.setCurrentBalance(updated.getCurrentBalance());
        existing.setCreditLimit(updated.getCreditLimit());
        existing.setCashCreditLimit(updated.getCashCreditLimit());
        existing.setOpenDate(updated.getOpenDate());
        existing.setExpirationDate(updated.getExpirationDate());
        existing.setReissueDate(updated.getReissueDate());
        existing.setCurrentCycleCredit(updated.getCurrentCycleCredit());
        existing.setCurrentCycleDebit(updated.getCurrentCycleDebit());
        existing.setAddressZip(updated.getAddressZip());
        existing.setGroupId(updated.getGroupId());
        return accountRepository.save(existing);
    }

    @Transactional
    public Account create(Account account) {
        return accountRepository.save(account);
    }
}
