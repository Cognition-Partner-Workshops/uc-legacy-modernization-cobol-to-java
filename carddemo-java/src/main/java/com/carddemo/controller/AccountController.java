package com.carddemo.controller;

import com.carddemo.dto.AccountDetailResponse;
import com.carddemo.dto.AccountUpdateDTO;
import com.carddemo.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Account controller - replaces COACTVWC (view) and COACTUPC (update).
 * Combines two COBOL programs (942 + 4237 lines) into two REST endpoints.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDetailResponse> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccountDetail(accountId));
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<AccountDetailResponse> updateAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountUpdateDTO dto) {
        return ResponseEntity.ok(accountService.updateAccount(accountId, dto));
    }
}
