package com.carddemo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Replaces CA00 admin menu transaction (COADM01C.cbl).
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/menu")
    public String adminMenu(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "admin-menu";
    }
}
