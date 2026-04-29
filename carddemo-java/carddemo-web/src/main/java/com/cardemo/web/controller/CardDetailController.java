package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COCRDSLC.cbl — Credit card detail view.
 * BMS Map: COCRDSL.bms
 *
 * TODO: Implement full business logic from COCRDSLC.cbl
 * TODO: Map all BMS screen fields from COCRDSL.bms to Thymeleaf model attributes
 */
@Controller
public class CardDetailController {

    @GetMapping("/cards/detail")
    public String show(Model model) {
        // TODO: Implement Credit card detail view logic from COCRDSLC.cbl
        return "cards-detail";
    }
}
