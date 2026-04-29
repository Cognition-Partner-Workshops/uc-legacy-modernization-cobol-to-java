package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COTRN02C.cbl — Transaction add screen.
 * BMS Map: COTRN02.bms
 *
 * TODO: Implement full business logic from COTRN02C.cbl
 * TODO: Map all BMS screen fields from COTRN02.bms to Thymeleaf model attributes
 */
@Controller
public class TransactionAddController {

    @GetMapping("/transactions/add")
    public String show(Model model) {
        // TODO: Implement Transaction add screen logic from COTRN02C.cbl
        return "transactions-add";
    }
}
