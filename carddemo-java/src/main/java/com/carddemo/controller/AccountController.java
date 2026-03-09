package com.carddemo.controller;

import com.carddemo.dto.AccountDto;
import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.service.online.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Account controller replacing COBOL programs COACTVWC and COACTUPC.
 *
 * <p>COACTVWC (Account View, CICS txn CAVW) -> GET /api/accounts/{id}
 * <p>COACTUPC (Account Update, CICS txn CAUP) -> PUT /api/accounts/{id}
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Account view and update (replaces COACTVWC/COACTUPC)")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "View account details",
            description = "Retrieve account information including linked customer data. "
                    + "Replaces COACTVWC (CICS txn CAVW). Performs VSAM reads on ACCTDAT, "
                    + "XREFFILE, and CUSTDAT to build the complete account view.")
    public ResponseEntity<AccountDto> getAccount(
            @Parameter(description = "Account ID (ACCT-ID, PIC 9(11))")
            @PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update account",
            description = "Update account fields. Replaces COACTUPC (CICS txn CAUP). "
                    + "Only non-null fields in the request body will be updated.")
    public ResponseEntity<AccountDto> updateAccount(
            @Parameter(description = "Account ID")
            @PathVariable Long id,
            @RequestBody AccountUpdateRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(id, request));
    }
}
