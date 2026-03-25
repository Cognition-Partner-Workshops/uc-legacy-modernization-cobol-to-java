package com.carddemo.api.controller;

import com.carddemo.common.dto.AccountDto;
import com.carddemo.api.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Account controller replacing COBOL programs:
 * - COACTVWC (CAVW transaction) — Account View
 * - COACTUPC (CAUP transaction) — Account Update
 *
 * Original COBOL: app/cbl/COACTVWC.cbl, app/cbl/COACTUPC.cbl
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * GET /api/accounts/{id} — View account details.
     * Replaces COACTVWC (CAVW transaction).
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable Long id) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COACTVWC");
    }

    /**
     * PUT /api/accounts/{id} — Update account.
     * Replaces COACTUPC (CAUP transaction).
     * Includes validation logic for credit limits, dates, SSN, etc.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(
            @PathVariable Long id,
            @RequestBody AccountDto accountDto) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COACTUPC");
    }
}
