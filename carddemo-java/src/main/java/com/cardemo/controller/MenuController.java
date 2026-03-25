package com.cardemo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Menu Controller - converted from COBOL programs COMEN01C.cbl and COADM01C.cbl
 * Original: CICS Main Menu (COMEN01C) and Admin Menu (COADM01C) screens
 * Replaces BMS map-based menu navigation with REST endpoints.
 * Menu options from COMEN02Y.cpy and COADM02Y.cpy copybooks.
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    /**
     * GET /api/menu/main
     * Main menu options - replaces COMEN01C / COMEN02Y.cpy.
     * Available to all authenticated users (type 'U' and 'A').
     */
    @GetMapping("/main")
    public ResponseEntity<Map<String, Object>> getMainMenu() {
        List<Map<String, String>> options = List.of(
                Map.of("option", "1", "name", "Account View", "endpoint", "/api/accounts/{acctId}"),
                Map.of("option", "2", "name", "Account Update", "endpoint", "/api/accounts/{acctId}"),
                Map.of("option", "3", "name", "Credit Card List", "endpoint", "/api/cards?acctId={acctId}"),
                Map.of("option", "4", "name", "Credit Card View", "endpoint", "/api/cards/{cardNum}"),
                Map.of("option", "5", "name", "Credit Card Update", "endpoint", "/api/cards/{cardNum}"),
                Map.of("option", "6", "name", "Transaction List", "endpoint", "/api/transactions/by-card?cardNum={cardNum}"),
                Map.of("option", "7", "name", "Transaction View", "endpoint", "/api/transactions/{tranId}"),
                Map.of("option", "8", "name", "Transaction Add", "endpoint", "/api/transactions"),
                Map.of("option", "9", "name", "Transaction Reports", "endpoint", "/api/reports/daily-transactions"),
                Map.of("option", "10", "name", "Bill Payment", "endpoint", "/api/payments")
        );

        return ResponseEntity.ok(Map.of(
                "title", "AWS Mainframe Modernization - CardDemo",
                "menuOptions", options
        ));
    }

    /**
     * GET /api/menu/admin
     * Admin menu options - replaces COADM01C / COADM02Y.cpy.
     * Available to admin users only (type 'A').
     */
    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAdminMenu() {
        List<Map<String, String>> options = List.of(
                Map.of("option", "1", "name", "User List (Security)", "endpoint", "/api/admin/users"),
                Map.of("option", "2", "name", "User Add (Security)", "endpoint", "/api/admin/users"),
                Map.of("option", "3", "name", "User Update (Security)", "endpoint", "/api/admin/users/{userId}"),
                Map.of("option", "4", "name", "User Delete (Security)", "endpoint", "/api/admin/users/{userId}")
        );

        return ResponseEntity.ok(Map.of(
                "title", "AWS Mainframe Modernization - CardDemo Admin",
                "menuOptions", options
        ));
    }
}
