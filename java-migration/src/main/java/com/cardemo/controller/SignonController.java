/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.controller;

import com.cardemo.model.CardDemoCommarea;
import com.cardemo.service.online.SignonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Signon Controller - REST API for user authentication.
 * Migrated from COBOL CICS program COSGN00C.cbl.
 */
@RestController
@RequestMapping("/api/auth")
public class SignonController {

    private final SignonService signonService;

    public SignonController(SignonService signonService) {
        this.signonService = signonService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String userId,
            @RequestParam String password) {
        try {
            CardDemoCommarea commarea = signonService.authenticate(userId, password);
            String nextProgram = signonService.getNextProgram(commarea);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Login successful",
                    "userId", commarea.getUserId(),
                    "userType", commarea.getUserType(),
                    "nextProgram", nextProgram));
        } catch (SignonService.SignonException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", "FAILED",
                    "message", e.getMessage()));
        }
    }
}
