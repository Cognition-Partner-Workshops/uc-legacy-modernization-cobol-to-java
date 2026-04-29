package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COSGN00C.cbl — Sign-on / Authentication screen.
 * BMS Map: COSGN00.bms
 *
 * TODO: Implement full business logic from COSGN00C.cbl
 * TODO: Map all BMS screen fields from COSGN00.bms to Thymeleaf model attributes
 */
@Controller
public class SignonController {

    @GetMapping("/login")
    public String show(Model model) {
        // TODO: Implement Sign-on / Authentication screen logic from COSGN00C.cbl
        return "login";
    }
}
