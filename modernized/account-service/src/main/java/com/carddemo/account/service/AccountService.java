package com.carddemo.account.service;

import com.carddemo.account.dto.AccountDto;
import com.carddemo.account.dto.BalanceAdjustmentRequest;
import com.carddemo.account.dto.UpdateAccountRequest;
import com.carddemo.account.model.Account;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.common.dto.PageResponse;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountDto getAccount(String acctId) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "acctId", acctId));
        return toDto(account);
    }

    @Transactional
    public AccountDto updateAccount(String acctId, UpdateAccountRequest request) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "acctId", acctId));

        if (request.getVersion() != null && !request.getVersion().equals(account.getVersion())) {
            throw new com.carddemo.account.exception.OptimisticLockException(
                    "Account " + acctId + " was modified by another transaction");
        }

        if (request.getActiveStatus() != null) {
            validateActiveStatus(request.getActiveStatus());
            account.setActiveStatus(request.getActiveStatus());
        }
        if (request.getCreditLimit() != null) {
            validateCreditLimit(request.getCreditLimit());
            account.setCreditLimit(request.getCreditLimit());
        }
        if (request.getCashCreditLimit() != null) {
            BigDecimal effectiveCreditLimit = request.getCreditLimit() != null
                    ? request.getCreditLimit() : account.getCreditLimit();
            validateCashCreditLimit(request.getCashCreditLimit(), effectiveCreditLimit);
            account.setCashCreditLimit(request.getCashCreditLimit());
        }
        if (request.getOpenDate() != null) {
            validateDateFormat(request.getOpenDate(), "openDate");
            account.setOpenDate(request.getOpenDate());
        }
        if (request.getExpirationDate() != null) {
            validateDateFormat(request.getExpirationDate(), "expirationDate");
            account.setExpirationDate(request.getExpirationDate());
        }
        if (request.getReissueDate() != null) {
            validateDateFormat(request.getReissueDate(), "reissueDate");
            account.setReissueDate(request.getReissueDate());
        }
        if (request.getGroupId() != null) {
            account.setGroupId(request.getGroupId());
        }
        if (request.getAddrZip() != null) {
            account.setAddrZip(request.getAddrZip());
        }

        try {
            account = accountRepository.save(account);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new com.carddemo.account.exception.OptimisticLockException(
                    "Account " + acctId + " was modified by another transaction");
        }
        return toDto(account);
    }

    @Transactional
    public AccountDto adjustBalance(String acctId, BalanceAdjustmentRequest request) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "acctId", acctId));

        BigDecimal currentBal = account.getCurrBal() != null ? account.getCurrBal() : BigDecimal.ZERO;
        account.setCurrBal(currentBal.add(request.getAmount()));

        account = accountRepository.save(account);
        return toDto(account);
    }

    public PageResponse<AccountDto> listAccounts(Pageable pageable) {
        Page<Account> page = accountRepository.findAll(pageable);
        List<AccountDto> content = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private void validateActiveStatus(Character status) {
        if (status != 'Y' && status != 'N') {
            throw new ValidationException("activeStatus must be 'Y' or 'N'");
        }
    }

    private void validateCreditLimit(BigDecimal creditLimit) {
        if (creditLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("creditLimit must be >= 0");
        }
    }

    private void validateCashCreditLimit(BigDecimal cashCreditLimit, BigDecimal creditLimit) {
        if (creditLimit != null && cashCreditLimit.compareTo(creditLimit) > 0) {
            throw new ValidationException("cashCreditLimit must be <= creditLimit");
        }
    }

    private void validateDateFormat(String date, String fieldName) {
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ValidationException(fieldName + " must be in YYYY-MM-DD format");
        }
    }

    private AccountDto toDto(Account a) {
        return AccountDto.builder()
                .acctId(a.getAcctId())
                .activeStatus(a.getActiveStatus())
                .currBal(a.getCurrBal())
                .creditLimit(a.getCreditLimit())
                .cashCreditLimit(a.getCashCreditLimit())
                .openDate(a.getOpenDate())
                .expirationDate(a.getExpirationDate())
                .reissueDate(a.getReissueDate())
                .currCycCredit(a.getCurrCycCredit())
                .currCycDebit(a.getCurrCycDebit())
                .addrZip(a.getAddrZip())
                .groupId(a.getGroupId())
                .version(a.getVersion())
                .build();
    }
}
