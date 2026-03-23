package com.carddemo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Replaces CM00 main menu transaction (COMEN01C.cbl).
 */
@Controller
public class MainMenuController {

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/menu";
        }
        return "redirect:/menu";
    }

    @GetMapping("/menu")
    public String mainMenu(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        return "main-menu";
    }
}
