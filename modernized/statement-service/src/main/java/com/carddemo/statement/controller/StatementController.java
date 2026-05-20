package com.carddemo.statement.controller;

import com.carddemo.statement.dto.StatementDto;
import com.carddemo.statement.service.StatementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statements")
public class StatementController {

    private final StatementService statementService;

    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }

    @GetMapping
    public ResponseEntity<List<StatementDto>> listStatements(@RequestParam String acctId) {
        return ResponseEntity.ok(statementService.listStatements(acctId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StatementDto> getStatement(@PathVariable Long id) {
        return ResponseEntity.ok(statementService.getStatement(id));
    }

    @PostMapping("/generate")
    public ResponseEntity<StatementDto> generateStatement(@RequestBody Map<String, String> request) {
        String acctId = request.get("acctId");
        LocalDate periodStart = LocalDate.parse(request.get("periodStart"));
        LocalDate periodEnd = LocalDate.parse(request.get("periodEnd"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(statementService.generateStatement(acctId, periodStart, periodEnd));
    }
}
