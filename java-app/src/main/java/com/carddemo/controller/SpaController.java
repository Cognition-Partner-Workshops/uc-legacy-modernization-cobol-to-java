package com.carddemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
        "/app",
        "/app/login",
        "/app/dashboard",
        "/app/accounts/**",
        "/app/cards/**",
        "/app/transactions/**",
        "/app/payments",
        "/app/reports",
        "/app/admin/**",
        "/app/batch"
    })
    public String forwardToReact() {
        return "forward:/app/index.html";
    }
}
