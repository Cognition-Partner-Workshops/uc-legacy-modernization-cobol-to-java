package com.carddemo.service.online;

import com.carddemo.dto.AccountDto;
import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.Customer;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Account service replacing COBOL programs COACTVWC and COACTUPC.
 *
 * <p>COACTVWC (Account View, CICS txn CAVW, 942 lines):
 * - Reads ACCTDAT by account ID
 * - Reads XREFFILE to find customer
 * - Reads CUSTDAT for customer details
 * - Displays combined account + customer info
 *
 * <p>COACTUPC (Account Update, CICS txn CAUP):
 * - Reads account record for update (EXEC CICS READ ... UPDATE)
 * - Validates changes
 * - Rewrites record (EXEC CICS REWRITE)
 */
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

    /**
     * View account details - replaces COACTVWC READ-ACCT-DATA paragraph.
     */
    @Transactional(readOnly = true)
    public AccountDto getAccount(Long acctId) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", String.valueOf(acctId)));

        AccountDto dto = mapToDto(account);

        // Lookup customer via xref (replaces VSAM AIX XREFFILE read)
        List<CardXref> xrefs = cardXrefRepository.findByXrefAcctId(acctId);
        if (!xrefs.isEmpty()) {
            Long custId = xrefs.get(0).getXrefCustId();
            customerRepository.findById(custId).ifPresent(customer -> {
                dto.setCustId(customer.getCustId());
                dto.setCustFirstName(customer.getCustFirstName());
                dto.setCustLastName(customer.getCustLastName());
            });
        }

        return dto;
    }

    /**
     * Update account - replaces COACTUPC PROCESS-ENTER-KEY paragraph.
     */
    @Transactional
    public AccountDto updateAccount(Long acctId, AccountUpdateRequest request) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", String.valueOf(acctId)));

        if (request.getAcctActiveStatus() != null) {
            account.setAcctActiveStatus(request.getAcctActiveStatus());
        }
        if (request.getAcctCreditLimit() != null) {
            account.setAcctCreditLimit(request.getAcctCreditLimit());
        }
        if (request.getAcctCashCreditLimit() != null) {
            account.setAcctCashCreditLimit(request.getAcctCashCreditLimit());
        }
        if (request.getAcctExpirationDate() != null) {
            account.setAcctExpirationDate(request.getAcctExpirationDate());
        }
        if (request.getAcctReissueDate() != null) {
            account.setAcctReissueDate(request.getAcctReissueDate());
        }
        if (request.getAcctAddrZip() != null) {
            account.setAcctAddrZip(request.getAcctAddrZip());
        }
        if (request.getAcctGroupId() != null) {
            account.setAcctGroupId(request.getAcctGroupId());
        }

        Account saved = accountRepository.save(account);
        return mapToDto(saved);
    }

    private AccountDto mapToDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setAcctId(account.getAcctId());
        dto.setAcctActiveStatus(account.getAcctActiveStatus());
        dto.setAcctCurrBal(account.getAcctCurrBal());
        dto.setAcctCreditLimit(account.getAcctCreditLimit());
        dto.setAcctCashCreditLimit(account.getAcctCashCreditLimit());
        dto.setAcctOpenDate(account.getAcctOpenDate());
        dto.setAcctExpirationDate(account.getAcctExpirationDate());
        dto.setAcctReissueDate(account.getAcctReissueDate());
        dto.setAcctCurrCycCredit(account.getAcctCurrCycCredit());
        dto.setAcctCurrCycDebit(account.getAcctCurrCycDebit());
        dto.setAcctAddrZip(account.getAcctAddrZip());
        dto.setAcctGroupId(account.getAcctGroupId());
        return dto;
    }
}
