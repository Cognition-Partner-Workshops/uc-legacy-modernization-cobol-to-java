package com.suitecrm.account.service;

import com.suitecrm.account.dto.*;
import com.suitecrm.account.entity.Account;
import com.suitecrm.account.mapper.AccountMapper;
import com.suitecrm.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional(readOnly = true)
    public Page<AccountDto> listAccounts(int page, int size, String sortBy, String sortDir, String search) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Account> accounts;
        if (search != null && !search.isBlank()) {
            accounts = accountRepository.searchAccounts(search, pageable);
        } else {
            accounts = accountRepository.findByDeletedFalse(pageable);
        }
        return accounts.map(accountMapper::toDto);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccount(UUID id) {
        Account account = accountRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
        return accountMapper.toDto(account);
    }

    public AccountDto createAccount(AccountCreateRequest request, UUID createdBy) {
        log.info("Creating account: name={}", request.getName());
        Account account = accountMapper.toEntity(request);
        account.setCreatedBy(createdBy);
        Account saved = accountRepository.save(account);
        log.info("Created account: id={}, name={}", saved.getId(), saved.getName());
        return accountMapper.toDto(saved);
    }

    public AccountDto updateAccount(UUID id, AccountUpdateRequest request) {
        log.info("Updating account: id={}", id);
        Account existing = accountRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
        accountMapper.updateEntity(existing, request);
        Account saved = accountRepository.save(existing);
        log.info("Updated account: id={}, name={}", saved.getId(), saved.getName());
        return accountMapper.toDto(saved);
    }

    public void deleteAccount(UUID id) {
        log.info("Soft-deleting account: id={}", id);
        Account account = accountRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
        account.setDeleted(true);
        accountRepository.save(account);
        log.info("Soft-deleted account: id={}", id);
    }

    @Transactional(readOnly = true)
    public Page<AccountDto> getAccountsByAssignedUser(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateModified").descending());
        return accountRepository.findByAssignedUserIdAndDeletedFalse(userId, pageable)
                .map(accountMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<AccountDto> getChildAccounts(UUID parentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return accountRepository.findByParentIdAndDeletedFalse(parentId, pageable)
                .map(accountMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countAccounts() {
        return accountRepository.countByDeletedFalse();
    }
}
