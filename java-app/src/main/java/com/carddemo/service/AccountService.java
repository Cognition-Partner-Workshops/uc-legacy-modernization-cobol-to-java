package com.carddemo.service;

import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.exception.AccountNotFoundException;
import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.Customer;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CardXrefRepository cardXrefRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
    }

    public Optional<Account> findById(Long acctId) {
        return accountRepository.findById(acctId);
    }

    public Account getAccount(Long acctId) {
        return accountRepository.findById(acctId)
                .orElseThrow(() -> new AccountNotFoundException(acctId));
    }

    public Map<String, Object> getAccountDetails(Long acctId) {
        Account account = getAccount(acctId);

        List<CardXref> xrefs = cardXrefRepository.findByAcctId(acctId);
        Long custId = xrefs.isEmpty() ? null : xrefs.get(0).getCustId();

        Customer customer = null;
        if (custId != null) {
            customer = customerRepository.findById(custId).orElse(null);
        }

        Map<String, Object> details = new HashMap<>();
        details.put("account", account);
        details.put("customer", customer);
        details.put("cardXrefs", xrefs);
        return details;
    }

    @Transactional
    public Account updateAccount(AccountUpdateRequest request) {
        Account account = getAccount(request.getAcctId());

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
        if (request.getGroupId() != null) {
            account.setGroupId(request.getGroupId());
        }

        return accountRepository.save(account);
    }

    @Transactional
    public void updateBalance(Long acctId, BigDecimal amount, boolean isCredit) {
        Account account = getAccount(acctId);
        account.setCurrBal(account.getCurrBal().add(amount));
        if (isCredit) {
            account.setCurrCycCredit(account.getCurrCycCredit().add(amount.abs()));
        } else {
            account.setCurrCycDebit(account.getCurrCycDebit().add(amount.abs()));
        }
        accountRepository.save(account);
    }

    public boolean validateAccountId(Long acctId) {
        return acctId != null && acctId > 0 && String.valueOf(acctId).length() <= 11;
    }
}
