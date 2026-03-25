/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.controller;

import com.cardemo.model.AccountRecord;
import com.cardemo.service.authorization.CardAuthorizationService;
import com.cardemo.service.authorization.CardAuthorizationService.AuthorizationRequest;
import com.cardemo.service.authorization.CardAuthorizationService.AuthorizationResponse;
import com.cardemo.service.authorization.AuthorizationSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authorization Controller - REST API for card authorization operations.
 * Migrated from COBOL CICS programs COPAUA0C.cbl, COPAUS0C.cbl, COPAUS1C.cbl.
 */
@RestController
@RequestMapping("/api/authorization")
public class AuthorizationController {

    private final CardAuthorizationService cardAuthorizationService;
    private final AuthorizationSummaryService authorizationSummaryService;

    public AuthorizationController(CardAuthorizationService cardAuthorizationService,
                                   AuthorizationSummaryService authorizationSummaryService) {
        this.cardAuthorizationService = cardAuthorizationService;
        this.authorizationSummaryService = authorizationSummaryService;
    }

    @PostMapping("/process")
    public ResponseEntity<AuthorizationResponse> processAuthorization(
            @RequestBody AuthorizationRequest request) {
        AuthorizationResponse response = cardAuthorizationService.processAuthorization(request);
        if ("APPROVED".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(403).body(response);
        }
    }

    @GetMapping("/account/{acctId}")
    public ResponseEntity<AccountRecord> getAccountForAuth(@PathVariable long acctId) {
        AccountRecord account = authorizationSummaryService.getAccountDetails(acctId);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account);
    }
}
