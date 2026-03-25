/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.controller;

import com.cardemo.model.AccountRecord;
import com.cardemo.service.online.AccountViewService;
import com.cardemo.service.online.AccountUpdateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Account Controller - REST API for account operations.
 * Migrated from COBOL CICS programs COACTVWC.cbl and COACTUPC.cbl.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountViewService accountViewService;
    private final AccountUpdateService accountUpdateService;

    public AccountController(AccountViewService accountViewService,
                             AccountUpdateService accountUpdateService) {
        this.accountViewService = accountViewService;
        this.accountUpdateService = accountUpdateService;
    }

    @GetMapping("/{acctId}")
    public ResponseEntity<Map<String, Object>> viewAccount(@PathVariable String acctId) {
        try {
            Map<String, Object> result = accountViewService.viewAccount(acctId);
            return ResponseEntity.ok(result);
        } catch (AccountViewService.AccountViewException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{acctId}")
    public ResponseEntity<Map<String, Object>> updateAccount(
            @PathVariable long acctId,
            @RequestBody AccountRecord account) {
        try {
            account.setAcctId(acctId);
            AccountRecord updated = accountUpdateService.updateAccount(account);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Account updated successfully",
                    "account", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }
}
