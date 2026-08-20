package com.carddemo.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @GetMapping("/menu")
    public Map<String, String> menu() {
        return Map.of("nextProgram", "COADM01C");
    }
}
