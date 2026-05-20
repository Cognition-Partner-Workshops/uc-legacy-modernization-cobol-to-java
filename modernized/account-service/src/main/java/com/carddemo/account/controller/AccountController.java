package com.carddemo.account.controller;

import com.carddemo.account.dto.AccountDto;
import com.carddemo.account.dto.BalanceAdjustmentRequest;
import com.carddemo.account.dto.UpdateAccountRequest;
import com.carddemo.account.exception.OptimisticLockException;
import com.carddemo.account.service.AccountService;
import com.carddemo.common.dto.ErrorResponse;
import com.carddemo.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AccountDto>> listAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(accountService.listAccounts(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable String id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable String id,
                                           @Valid @RequestBody UpdateAccountRequest request) {
        try {
            return ResponseEntity.ok(accountService.updateAccount(id, request));
        } catch (OptimisticLockException e) {
            ErrorResponse error = new ErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.CONFLICT.value(),
                    e.getMessage(),
                    "/api/accounts/" + id
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    @PatchMapping("/{id}/balance")
    public ResponseEntity<AccountDto> adjustBalance(@PathVariable String id,
                                                     @Valid @RequestBody BalanceAdjustmentRequest request) {
        return ResponseEntity.ok(accountService.adjustBalance(id, request));
    }
}
