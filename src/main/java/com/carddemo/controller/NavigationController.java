package com.carddemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class NavigationController {

    @GetMapping("/api")
    public Map<String, String> root() {
        return Map.of(
                "accounts", "/api/accounts",
                "customers", "/api/customers",
                "transactions", "/api/transactions",
                "cards", "/api/cards",
                "payments", "/api/payments",
                "users", "/api/users (admin only)"
        );
    }
}
