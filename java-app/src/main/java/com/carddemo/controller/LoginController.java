package com.carddemo.controller;

import com.carddemo.util.DateTimeUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("currentDate", DateTimeUtil.getCurrentDateFormatted());
        model.addAttribute("currentTime", DateTimeUtil.getCurrentTimeFormatted());
        model.addAttribute("programName", "COSGN00C");
        return "login";
    }
}
