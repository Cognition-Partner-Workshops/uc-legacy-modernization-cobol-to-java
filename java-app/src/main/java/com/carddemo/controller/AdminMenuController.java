package com.carddemo.controller;

import com.carddemo.util.DateTimeUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminMenuController {

    @GetMapping("/menu")
    public String showAdminMenu(Model model, Authentication authentication) {
        model.addAttribute("currentDate", DateTimeUtil.getCurrentDateFormatted());
        model.addAttribute("currentTime", DateTimeUtil.getCurrentTimeFormatted());
        model.addAttribute("programName", "COADM01C");
        model.addAttribute("userId", authentication.getName());
        return "admin-menu";
    }

    @PostMapping("/menu")
    public String processAdminMenu(@RequestParam("option") int option) {
        return switch (option) {
            case 1 -> "redirect:/admin/users";
            case 2 -> "redirect:/admin/users/add";
            case 3 -> "redirect:/admin/users/update";
            case 4 -> "redirect:/admin/users/delete";
            default -> "redirect:/admin/menu";
        };
    }
}
