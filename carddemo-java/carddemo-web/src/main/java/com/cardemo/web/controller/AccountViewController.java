package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COACTVWC.cbl — Account view screen.
 * BMS Map: COACTVW.bms
 *
 * TODO: Implement full business logic from COACTVWC.cbl
 * TODO: Map all BMS screen fields from COACTVW.bms to Thymeleaf model attributes
 */
@Controller
public class AccountViewController {

    @GetMapping("/accounts/view")
    public String show(Model model) {
        // TODO: Implement Account view screen logic from COACTVWC.cbl
        return "accounts-view";
    }
}
