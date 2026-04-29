package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COCRDLIC.cbl — Credit card list screen.
 * BMS Map: COCRDLI.bms
 *
 * TODO: Implement full business logic from COCRDLIC.cbl
 * TODO: Map all BMS screen fields from COCRDLI.bms to Thymeleaf model attributes
 */
@Controller
public class CardListController {

    @GetMapping("/cards")
    public String show(Model model) {
        // TODO: Implement Credit card list screen logic from COCRDLIC.cbl
        return "cards";
    }
}
