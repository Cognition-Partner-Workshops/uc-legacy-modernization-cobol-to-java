package com.carddemo.controller;

import com.carddemo.util.DateTimeUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainMenuController {

    @GetMapping("/menu")
    public String showMenu(Model model, Authentication authentication) {
        populateHeader(model, authentication);
        return "menu";
    }

    @PostMapping("/menu")
    public String processMenu(@RequestParam("option") int option, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return switch (option) {
            case 1 -> "redirect:/accounts/view";
            case 2 -> "redirect:/accounts/update";
            case 3 -> "redirect:/cards/list";
            case 4 -> "redirect:/cards/view";
            case 5 -> "redirect:/cards/update";
            case 6 -> "redirect:/transactions";
            case 7 -> "redirect:/transactions/view";
            case 8 -> "redirect:/transactions/add";
            case 9 -> "redirect:/reports";
            case 10 -> "redirect:/payments";
            default -> "redirect:/menu";
        };
    }

    private void populateHeader(Model model, Authentication authentication) {
        model.addAttribute("currentDate", DateTimeUtil.getCurrentDateFormatted());
        model.addAttribute("currentTime", DateTimeUtil.getCurrentTimeFormatted());
        model.addAttribute("programName", "COMEN01C");
        model.addAttribute("userId", authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
    }
}
