package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COACTUPC.cbl — Account update screen.
 * BMS Map: COACTUP.bms
 *
 * TODO: Implement full business logic from COACTUPC.cbl
 * TODO: Map all BMS screen fields from COACTUP.bms to Thymeleaf model attributes
 */
@Controller
public class AccountUpdateController {

    @GetMapping("/accounts/update")
    public String show(Model model) {
        // TODO: Implement Account update screen logic from COACTUPC.cbl
        return "accounts-update";
    }
}
