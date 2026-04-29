package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COUSR01C.cbl — User add screen.
 * BMS Map: COUSR01.bms
 *
 * TODO: Implement full business logic from COUSR01C.cbl
 * TODO: Map all BMS screen fields from COUSR01.bms to Thymeleaf model attributes
 */
@Controller
public class UserAddController {

    @GetMapping("/admin/users/add")
    public String show(Model model) {
        // TODO: Implement User add screen logic from COUSR01C.cbl
        return "admin-users-add";
    }
}
