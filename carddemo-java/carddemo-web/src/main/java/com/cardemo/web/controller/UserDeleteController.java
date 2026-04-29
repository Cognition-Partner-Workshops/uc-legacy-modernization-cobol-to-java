package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COUSR03C.cbl — User delete screen.
 * BMS Map: COUSR03.bms
 *
 * TODO: Implement full business logic from COUSR03C.cbl
 * TODO: Map all BMS screen fields from COUSR03.bms to Thymeleaf model attributes
 */
@Controller
public class UserDeleteController {

    @GetMapping("/admin/users/delete")
    public String show(Model model) {
        // TODO: Implement User delete screen logic from COUSR03C.cbl
        return "admin-users-delete";
    }
}
