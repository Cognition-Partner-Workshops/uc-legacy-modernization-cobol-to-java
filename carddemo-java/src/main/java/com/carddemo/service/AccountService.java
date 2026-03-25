package com.carddemo.service;

import com.carddemo.dto.AccountDetailResponse;
import com.carddemo.dto.AccountUpdateDTO;
import com.carddemo.entity.Account;
import com.carddemo.entity.Card;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Customer;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.util.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Account service - consolidates COACTVWC (view) and COACTUPC (update).
 * Both COBOL programs share identical file reads from ACCTDAT, CUSTDAT, CARDXREF.
 * This service eliminates that duplication with a shared getAccountDetail method.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository,
                          CardRepository cardRepository,
                          CardXrefRepository cardXrefRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * Get account detail with customer info and cards.
     * Replaces COACTVWC which reads ACCTDAT, then CARDXREF to find customer, then CUSTDAT.
     */
    public AccountDetailResponse getAccountDetail(Long accountId) {
        Account account = findAccountOrThrow(accountId);
        Customer customer = findCustomerForAccount(accountId);
        List<Card> cards = cardRepository.findByAccountId(accountId);
        return AccountDetailResponse.fromEntities(account, customer, cards);
    }

    /**
     * Update account and associated customer.
     * Replaces COACTUPC's 4237-line program with Bean Validation + simple field updates.
     */
    @Transactional
    public AccountDetailResponse updateAccount(Long accountId, AccountUpdateDTO dto) {
        Account account = findAccountOrThrow(accountId);

        if (dto.getActiveStatus() != null) account.setActiveStatus(dto.getActiveStatus());
        if (dto.getCreditLimit() != null) account.setCreditLimit(dto.getCreditLimit());
        if (dto.getCashCreditLimit() != null) account.setCashCreditLimit(dto.getCashCreditLimit());
        if (dto.getExpirationDate() != null) account.setExpirationDate(DateUtils.parseDate(dto.getExpirationDate()));
        if (dto.getReissueDate() != null) account.setReissueDate(DateUtils.parseDate(dto.getReissueDate()));
        if (dto.getZipCode() != null) account.setZipCode(dto.getZipCode());
        if (dto.getGroupId() != null) account.setGroupId(dto.getGroupId());

        accountRepository.save(account);

        Customer customer = findCustomerForAccount(accountId);
        if (customer != null) {
            updateCustomerFields(customer, dto);
            customerRepository.save(customer);
        }

        List<Card> cards = cardRepository.findByAccountId(accountId);
        return AccountDetailResponse.fromEntities(account, customer, cards);
    }

    private void updateCustomerFields(Customer customer, AccountUpdateDTO dto) {
        if (dto.getFirstName() != null) customer.setFirstName(dto.getFirstName());
        if (dto.getMiddleName() != null) customer.setMiddleName(dto.getMiddleName());
        if (dto.getLastName() != null) customer.setLastName(dto.getLastName());
        if (dto.getAddrLine1() != null) customer.setAddrLine1(dto.getAddrLine1());
        if (dto.getAddrLine2() != null) customer.setAddrLine2(dto.getAddrLine2());
        if (dto.getAddrLine3() != null) customer.setAddrLine3(dto.getAddrLine3());
        if (dto.getStateCode() != null) customer.setStateCode(dto.getStateCode());
        if (dto.getCountryCode() != null) customer.setCountryCode(dto.getCountryCode());
        if (dto.getPhone1() != null) customer.setPhone1(dto.getPhone1());
        if (dto.getPhone2() != null) customer.setPhone2(dto.getPhone2());
        if (dto.getSsn() != null) customer.setSsn(dto.getSsn());
        if (dto.getGovtId() != null) customer.setGovtId(dto.getGovtId());
        if (dto.getDateOfBirth() != null) customer.setDateOfBirth(DateUtils.parseDate(dto.getDateOfBirth()));
        if (dto.getEftAccountId() != null) customer.setEftAccountId(dto.getEftAccountId());
    }

    private Account findAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
    }

    private Customer findCustomerForAccount(Long accountId) {
        List<CardXref> xrefs = cardXrefRepository.findByAccountId(accountId);
        if (xrefs.isEmpty()) return null;
        return customerRepository.findById(xrefs.get(0).getCustomerId()).orElse(null);
    }
}
