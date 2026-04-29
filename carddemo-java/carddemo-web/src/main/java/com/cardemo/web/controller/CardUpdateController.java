package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COCRDUPC.cbl — Credit card update screen.
 * BMS Map: COCRDUP.bms
 *
 * TODO: Implement full business logic from COCRDUPC.cbl
 * TODO: Map all BMS screen fields from COCRDUP.bms to Thymeleaf model attributes
 */
@Controller
public class CardUpdateController {

    @GetMapping("/cards/update")
    public String show(Model model) {
        // TODO: Implement Credit card update screen logic from COCRDUPC.cbl
        return "cards-update";
    }
}
