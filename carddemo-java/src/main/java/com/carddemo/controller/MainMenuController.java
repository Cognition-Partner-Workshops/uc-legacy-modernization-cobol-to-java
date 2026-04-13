package com.carddemo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main Menu Controller - maps to COMEN01C/CM00
 * Screen from app/bms/COMEN01.bms
 * Menu options from COMEN02Y.cpy
 */
@Controller
public class MainMenuController {

    private static final List<Map<String, String>> ALL_MENU_OPTIONS = List.of(
        Map.of("num", "1", "name", "Account View", "program", "COACTVWC", "url", "/accounts?action=view", "minType", "U"),
        Map.of("num", "2", "name", "Account Update", "program", "COACTUPC", "url", "/accounts?action=update", "minType", "U"),
        Map.of("num", "3", "name", "Credit Card List", "program", "COCRDLIC", "url", "/cards", "minType", "U"),
        Map.of("num", "4", "name", "Credit Card View", "program", "COCRDSLC", "url", "/cards/search", "minType", "U"),
        Map.of("num", "5", "name", "Credit Card Update", "program", "COCRDUPC", "url", "/cards?action=update", "minType", "U"),
        Map.of("num", "6", "name", "Transaction List", "program", "COTRN00C", "url", "/transactions", "minType", "U"),
        Map.of("num", "7", "name", "Transaction View", "program", "COTRN01C", "url", "/transactions?action=view", "minType", "U"),
        Map.of("num", "8", "name", "Transaction Add", "program", "COTRN02C", "url", "/transactions/add", "minType", "U"),
        Map.of("num", "9", "name", "Transaction Reports", "program", "CORPT00C", "url", "/reports", "minType", "U"),
        Map.of("num", "10", "name", "Bill Payment", "program", "COBIL00C", "url", "/payments", "minType", "U"),
        Map.of("num", "11", "name", "Pending Authorization View", "program", "COPAUS0C", "url", "#", "minType", "U")
    );

    @GetMapping("/menu")
    public String showMainMenu(Authentication authentication, Model model) {
        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        List<Map<String, String>> menuOptions = new ArrayList<>(ALL_MENU_OPTIONS);

        model.addAttribute("menuOptions", menuOptions);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("userId", authentication.getName());
        model.addAttribute("tranName", "CM00");
        model.addAttribute("pgmName", "COMEN01C");
        return "main-menu";
    }
}
