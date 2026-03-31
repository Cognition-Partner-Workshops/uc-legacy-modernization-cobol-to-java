package com.carddemo.controller.api;

import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.model.Account;
import com.carddemo.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountApiController {

    private final AccountService accountService;

    public AccountApiController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{acctId}")
    public ResponseEntity<Map<String, Object>> getAccountDetails(@PathVariable Long acctId) {
        if (!accountService.validateAccountId(acctId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid Account ID. Must be a non-zero numeric value up to 11 digits."));
        }
        Map<String, Object> details = accountService.getAccountDetails(acctId);
        return ResponseEntity.ok(details);
    }

    @PutMapping("/{acctId}")
    public ResponseEntity<?> updateAccount(@PathVariable Long acctId, @RequestBody AccountUpdateRequest request) {
        request.setAcctId(acctId);
        Account updated = accountService.updateAccount(request);
        return ResponseEntity.ok(updated);
    }
}
