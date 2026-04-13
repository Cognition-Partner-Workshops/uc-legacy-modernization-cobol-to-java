package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.CreditCard;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CreditCardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CreditCardRepository creditCardRepository;

    public AccountService(AccountRepository accountRepository,
                          CardXrefRepository cardXrefRepository,
                          CreditCardRepository creditCardRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.creditCardRepository = creditCardRepository;
    }

    /**
     * View account by ID - mirrors COACTVWC.cbl
     * Reads account record and fetches associated cards via CardXref
     */
    public Account viewAccount(Long acctId) {
        return accountRepository.findById(acctId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + acctId));
    }

    public List<CreditCard> getCardsForAccount(Long acctId) {
        return creditCardRepository.findByAcctId(acctId);
    }

    public List<CardXref> getXrefsForAccount(Long acctId) {
        return cardXrefRepository.findByAcctId(acctId);
    }

    /**
     * Update account - mirrors COACTUPC.cbl
     * Validates and updates account record fields
     */
    @Transactional
    public Account updateAccount(Long acctId, Account updatedFields) {
        Account existing = accountRepository.findById(acctId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + acctId));

        if (updatedFields.getActiveStatus() != null) {
            existing.setActiveStatus(updatedFields.getActiveStatus());
        }
        if (updatedFields.getCreditLimit() != null) {
            existing.setCreditLimit(updatedFields.getCreditLimit());
        }
        if (updatedFields.getCashCreditLimit() != null) {
            existing.setCashCreditLimit(updatedFields.getCashCreditLimit());
        }
        if (updatedFields.getExpirationDate() != null) {
            existing.setExpirationDate(updatedFields.getExpirationDate());
        }
        if (updatedFields.getReissueDate() != null) {
            existing.setReissueDate(updatedFields.getReissueDate());
        }
        if (updatedFields.getGroupId() != null) {
            existing.setGroupId(updatedFields.getGroupId());
        }

        return accountRepository.save(existing);
    }

    /**
     * Paginated account listing
     */
    public Page<Account> listAccounts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accountRepository.findAll(pageable);
    }

    /**
     * Update account balance - used by batch jobs and bill payment
     */
    @Transactional
    public void updateBalance(Long acctId, BigDecimal amount) {
        Account account = viewAccount(acctId);
        BigDecimal newBalance = account.getCurrentBalance().add(amount);
        account.setCurrentBalance(newBalance);
        accountRepository.save(account);
    }
}
