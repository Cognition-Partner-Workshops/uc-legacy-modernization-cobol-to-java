package com.cardemo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller replacing COBOL program COUSR00C.cbl — User list screen.
 * BMS Map: COUSR00.bms
 *
 * TODO: Implement full business logic from COUSR00C.cbl
 * TODO: Map all BMS screen fields from COUSR00.bms to Thymeleaf model attributes
 */
@Controller
public class UserListController {

    @GetMapping("/admin/users")
    public String show(Model model) {
        // TODO: Implement User list screen logic from COUSR00C.cbl
        return "admin-users";
    }
}
