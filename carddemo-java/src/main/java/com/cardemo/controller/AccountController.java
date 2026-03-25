package com.cardemo.controller;

import com.cardemo.model.Account;
import com.cardemo.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Account Controller - converted from COBOL programs COACTVWC.cbl and COACTUPC.cbl
 * Original: CICS Account View and Account Update screens
 * Replaces BMS maps COACTVW/COACTUP with REST endpoints.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * GET /api/accounts
     * Lists all accounts.
     */
    @GetMapping
    public ResponseEntity<List<Account>> listAccounts() {
        return ResponseEntity.ok(accountService.listAccounts());
    }

    /**
     * GET /api/accounts/{acctId}
     * View account details - replaces COACTVWC SEND-ACTVW-SCREEN.
     */
    @GetMapping("/{acctId}")
    public ResponseEntity<Account> viewAccount(@PathVariable Long acctId) {
        return accountService.viewAccount(acctId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/accounts/{acctId}
     * Update account - replaces COACTUPC PROCESS-ENTER-KEY.
     */
    @PutMapping("/{acctId}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long acctId,
                                                 @RequestBody Account updatedData) {
        return accountService.updateAccount(acctId, updatedData)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
