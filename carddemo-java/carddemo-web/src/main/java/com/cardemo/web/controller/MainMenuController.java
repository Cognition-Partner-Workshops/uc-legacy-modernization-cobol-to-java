package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COMEN01C.cbl — Main menu navigation.
 * BMS Map: COMEN01.bms
 *
 * TODO: Implement full business logic from COMEN01C.cbl
 * TODO: Map all BMS screen fields from COMEN01.bms to Thymeleaf model attributes
 */
@Controller
public class MainMenuController {

    @GetMapping("/menu")
    public String show(Model model) {
        // TODO: Implement Main menu navigation logic from COMEN01C.cbl
        return "menu";
    }
}
