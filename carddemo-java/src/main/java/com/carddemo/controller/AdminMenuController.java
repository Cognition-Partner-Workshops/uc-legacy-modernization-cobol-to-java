package com.carddemo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Admin Menu Controller - maps to COADM01C/CA00
 * Screen from app/bms/COADM01.bms
 */
@Controller
public class AdminMenuController {

    @GetMapping("/admin")
    public String showAdminMenu(Authentication authentication, Model model) {
        model.addAttribute("userId", authentication.getName());
        model.addAttribute("tranName", "CA00");
        model.addAttribute("pgmName", "COADM01C");
        return "admin-menu";
    }
}
