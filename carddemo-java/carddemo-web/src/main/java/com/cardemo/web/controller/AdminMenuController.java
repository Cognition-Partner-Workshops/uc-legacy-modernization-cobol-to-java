package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COADM01C.cbl — Admin menu screen.
 * BMS Map: COADM01.bms
 *
 * TODO: Implement full business logic from COADM01C.cbl
 * TODO: Map all BMS screen fields from COADM01.bms to Thymeleaf model attributes
 */
@Controller
public class AdminMenuController {

    @GetMapping("/admin/menu")
    public String show(Model model) {
        // TODO: Implement Admin menu screen logic from COADM01C.cbl
        return "admin-menu";
    }
}
