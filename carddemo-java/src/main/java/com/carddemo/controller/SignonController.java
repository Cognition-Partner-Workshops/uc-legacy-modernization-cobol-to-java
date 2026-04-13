package com.carddemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Signon Controller - maps to COSGN00C/CC00
 * Screen from app/bms/COSGN00.bms
 */
@Controller
public class SignonController {

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid User ID or Password. Try again ...");
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "Thank you for using CardDemo. Goodbye.");
        }
        model.addAttribute("tranName", "CC00");
        model.addAttribute("pgmName", "COSGN00C");
        return "signon";
    }
}
