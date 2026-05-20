package com.carddemo.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @GetMapping("/date")
    public ResponseEntity<Map<String, String>> getSystemDate() {
        return ResponseEntity.ok(Map.of("date", LocalDate.now().toString()));
    }
}
