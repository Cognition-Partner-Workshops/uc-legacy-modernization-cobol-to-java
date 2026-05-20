package com.carddemo.authorization.controller;

import com.carddemo.authorization.dto.AuthorizationRequest;
import com.carddemo.authorization.dto.AuthorizationResponse;
import com.carddemo.authorization.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authorizations")
@RequiredArgsConstructor
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    @PostMapping("/process")
    public ResponseEntity<AuthorizationResponse> process(@RequestBody AuthorizationRequest request) {
        return ResponseEntity.ok(authorizationService.processAuthorization(request));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AuthorizationResponse>> pending(@RequestParam String cardNum) {
        return ResponseEntity.ok(authorizationService.getPendingByCard(cardNum));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorizationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(authorizationService.getAuthorization(id));
    }

    @DeleteMapping("/purge")
    public ResponseEntity<Void> purge() {
        authorizationService.purgeExpired();
        return ResponseEntity.noContent().build();
    }
}
