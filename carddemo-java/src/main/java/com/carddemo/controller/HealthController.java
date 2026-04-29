package com.carddemo.controller;

import com.carddemo.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .application("CardDemo")
                .version("0.0.1-SNAPSHOT")
                .build();
        return ResponseEntity.ok(response);
    }
}
