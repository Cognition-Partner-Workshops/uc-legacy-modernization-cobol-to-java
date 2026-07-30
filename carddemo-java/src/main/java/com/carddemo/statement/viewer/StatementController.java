package com.carddemo.statement.viewer;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statements")
public class StatementController {
    private final StatementRepository repository;

    public StatementController(StatementRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Statement> list() {
        return repository.findAll();
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Statement> get(@PathVariable String accountId) {
        return repository.findByAccountId(accountId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
