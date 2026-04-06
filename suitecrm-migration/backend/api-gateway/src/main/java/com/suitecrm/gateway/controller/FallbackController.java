package com.suitecrm.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authFallback() {
        return createFallbackResponse("Authentication service is temporarily unavailable");
    }

    @GetMapping("/contact")
    public Mono<ResponseEntity<Map<String, Object>>> contactFallback() {
        return createFallbackResponse("Contact service is temporarily unavailable");
    }

    @GetMapping("/account")
    public Mono<ResponseEntity<Map<String, Object>>> accountFallback() {
        return createFallbackResponse("Account service is temporarily unavailable");
    }

    @GetMapping("/opportunity")
    public Mono<ResponseEntity<Map<String, Object>>> opportunityFallback() {
        return createFallbackResponse("Opportunity service is temporarily unavailable");
    }

    @GetMapping("/case")
    public Mono<ResponseEntity<Map<String, Object>>> caseFallback() {
        return createFallbackResponse("Case service is temporarily unavailable");
    }

    @GetMapping("/campaign")
    public Mono<ResponseEntity<Map<String, Object>>> campaignFallback() {
        return createFallbackResponse("Campaign service is temporarily unavailable");
    }

    @GetMapping("/activity")
    public Mono<ResponseEntity<Map<String, Object>>> activityFallback() {
        return createFallbackResponse("Activity service is temporarily unavailable");
    }

    @GetMapping("/report")
    public Mono<ResponseEntity<Map<String, Object>>> reportFallback() {
        return createFallbackResponse("Report service is temporarily unavailable");
    }

    @GetMapping("/document")
    public Mono<ResponseEntity<Map<String, Object>>> documentFallback() {
        return createFallbackResponse("Document service is temporarily unavailable");
    }

    @GetMapping("/workflow")
    public Mono<ResponseEntity<Map<String, Object>>> workflowFallback() {
        return createFallbackResponse("Workflow service is temporarily unavailable");
    }

    private Mono<ResponseEntity<Map<String, Object>>> createFallbackResponse(String message) {
        Map<String, Object> response = Map.of(
                "status", "SERVICE_UNAVAILABLE",
                "message", message,
                "timestamp", LocalDateTime.now().toString()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}
