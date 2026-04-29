package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COTRN00C.cbl — Transaction list screen.
 * BMS Map: COTRN00.bms
 *
 * TODO: Implement full business logic from COTRN00C.cbl
 * TODO: Map all BMS screen fields from COTRN00.bms to Thymeleaf model attributes
 */
@Controller
public class TransactionListController {

    @GetMapping("/transactions")
    public String show(Model model) {
        // TODO: Implement Transaction list screen logic from COTRN00C.cbl
        return "transactions";
    }
}
