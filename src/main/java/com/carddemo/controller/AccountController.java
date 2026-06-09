package com.carddemo.controller;

import com.carddemo.model.Account;
import com.carddemo.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public Page<Account> listAccounts(Pageable pageable) {
        return accountService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Account getAccount(@PathVariable long id) {
        return accountService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account) {
        Account created = accountService.create(account);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public Account updateAccount(@PathVariable long id, @Valid @RequestBody Account account) {
        return accountService.update(id, account);
    }
}
