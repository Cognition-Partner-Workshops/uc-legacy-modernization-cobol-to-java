package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program CORPT00C.cbl — Transaction report screen.
 * BMS Map: CORPT00.bms
 *
 * TODO: Implement full business logic from CORPT00C.cbl
 * TODO: Map all BMS screen fields from CORPT00.bms to Thymeleaf model attributes
 */
@Controller
public class ReportController {

    @GetMapping("/reports")
    public String show(Model model) {
        // TODO: Implement Transaction report screen logic from CORPT00C.cbl
        return "reports";
    }
}
