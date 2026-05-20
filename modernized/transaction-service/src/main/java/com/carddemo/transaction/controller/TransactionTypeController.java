package com.carddemo.transaction.controller;

import com.carddemo.transaction.dto.TransactionTypeDto;
import com.carddemo.transaction.service.TransactionTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transaction-types")
public class TransactionTypeController {

    private final TransactionTypeService service;

    public TransactionTypeController(TransactionTypeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TransactionTypeDto>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @PostMapping
    public ResponseEntity<TransactionTypeDto> create(@Valid @RequestBody TransactionTypeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{code}")
    public ResponseEntity<TransactionTypeDto> update(@PathVariable String code,
                                                      @Valid @RequestBody TransactionTypeDto dto) {
        return ResponseEntity.ok(service.update(code, dto));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        service.delete(code);
        return ResponseEntity.noContent().build();
    }
}
