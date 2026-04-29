package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COBIL00C.cbl — Bill payment screen.
 * BMS Map: COBIL00.bms
 *
 * TODO: Implement full business logic from COBIL00C.cbl
 * TODO: Map all BMS screen fields from COBIL00.bms to Thymeleaf model attributes
 */
@Controller
public class BillPaymentController {

    @GetMapping("/billing")
    public String show(Model model) {
        // TODO: Implement Bill payment screen logic from COBIL00C.cbl
        return "billing";
    }
}
