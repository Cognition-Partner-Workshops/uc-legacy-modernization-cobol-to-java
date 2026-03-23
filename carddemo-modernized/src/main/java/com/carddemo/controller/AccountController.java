package com.carddemo.controller;

import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.model.Account;
import com.carddemo.model.CardCrossReference;
import com.carddemo.model.Customer;
import com.carddemo.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Account controller - replaces COBOL programs:
 *   COACTVWC.cbl (CAVW transaction) -> GET  /api/accounts/{id}
 *   COACTUPC.cbl (CAUP transaction) -> PUT  /api/accounts/{id}
 *
 * Legacy CICS flow (COACTVWC - Account View):
 *   1. User enters account ID on screen
 *   2. Program reads CARDXREF via alternate index (CXACAIX)
 *   3. Reads ACCTDAT for account details
 *   4. Reads CUSTDAT for customer info
 *   5. Populates BMS map CACTVWA and sends to terminal
 *
 * Modernized: Reactive REST endpoints with Cassandra backend
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public Flux<Account> listAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{accountId}")
    public Mono<Account> viewAccount(@PathVariable String accountId) {
        return accountService.getAccountById(accountId);
    }

    @GetMapping("/{accountId}/cross-references")
    public Flux<CardCrossReference> getCardCrossReferences(@PathVariable String accountId) {
        return accountService.getCardCrossReferences(accountId);
    }

    @GetMapping("/{accountId}/customer")
    public Mono<Customer> getCustomerForAccount(@PathVariable String accountId) {
        return accountService.getCustomerForAccount(accountId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Account> createAccount(@Valid @RequestBody Account account) {
        return accountService.createAccount(account);
    }

    @PutMapping("/{accountId}")
    public Mono<Account> updateAccount(@PathVariable String accountId,
                                       @Valid @RequestBody AccountUpdateRequest request) {
        return accountService.updateAccount(accountId, request);
    }
}
