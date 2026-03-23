package com.carddemo.service;

import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.exception.BusinessValidationException;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.Account;
import com.carddemo.model.CardCrossReference;
import com.carddemo.model.Customer;
import com.carddemo.repository.cassandra.AccountRepository;
import com.carddemo.repository.cassandra.CardCrossReferenceRepository;
import com.carddemo.repository.cassandra.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Account service - modernized business logic from COBOL programs:
 *
 *   COACTVWC.cbl  -> viewAccount()     (Account View - CAVW transaction)
 *   COACTUPC.cbl  -> updateAccount()   (Account Update - CAUP transaction)
 *   CBACT01C.cbl  -> batch account processing logic
 *   CBACT03C.cbl  -> account listing
 *   CBACT04C.cbl  -> interest calculation (calculateInterest)
 *
 * Legacy flow (COACTVWC):
 *   1. Receive account ID from BMS screen
 *   2. Read CARDXREF (cross-reference) by account ID via alternate index
 *   3. Read ACCTDAT (account master) by account ID
 *   4. Read CUSTDAT (customer master) by customer ID from cross-ref
 *   5. Populate screen fields and send map
 *
 * Modernized: Reactive Cassandra queries with Mono/Flux composition
 */
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final CardCrossReferenceRepository crossRefRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CardCrossReferenceRepository crossRefRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.crossRefRepository = crossRefRepository;
        this.customerRepository = customerRepository;
    }

    public Mono<Account> getAccountById(String accountId) {
        log.debug("Viewing account: {}", accountId);
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Did not find this account in account master file")));
    }

    public Flux<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Flux<Account> getActiveAccounts() {
        return accountRepository.findByActiveStatus("Y");
    }

    public Mono<Account> updateAccount(String accountId, AccountUpdateRequest request) {
        log.debug("Updating account: {}", accountId);
        return accountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Did not find this account in account master file")))
                .flatMap(existing -> {
                    if (request.getActiveStatus() != null) {
                        existing.setActiveStatus(request.getActiveStatus());
                    }
                    if (request.getCreditLimit() != null) {
                        existing.setCreditLimit(request.getCreditLimit());
                    }
                    if (request.getCashCreditLimit() != null) {
                        existing.setCashCreditLimit(request.getCashCreditLimit());
                    }
                    if (request.getAddressZip() != null) {
                        existing.setAddressZip(request.getAddressZip());
                    }
                    if (request.getGroupId() != null) {
                        existing.setGroupId(request.getGroupId());
                    }
                    return accountRepository.save(existing);
                });
    }

    public Mono<Account> createAccount(Account account) {
        log.debug("Creating account: {}", account.getAccountId());
        if (account.getAccountId() == null || account.getAccountId().isBlank()) {
            return Mono.error(new BusinessValidationException(
                    "Account number must be a non-zero 11 digit number"));
        }
        return accountRepository.save(account);
    }

    public Flux<CardCrossReference> getCardCrossReferences(String accountId) {
        return crossRefRepository.findByAccountId(accountId);
    }

    public Mono<Customer> getCustomerForAccount(String accountId) {
        return crossRefRepository.findByAccountId(accountId)
                .next()
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Did not find this account in account card xref file")))
                .flatMap(xref -> customerRepository.findById(xref.getCustomerId())
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                                "Did not find associated customer in master file"))));
    }
}
