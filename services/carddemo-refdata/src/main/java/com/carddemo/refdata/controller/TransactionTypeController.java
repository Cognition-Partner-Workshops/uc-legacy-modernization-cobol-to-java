package com.carddemo.refdata.controller;

import java.util.List;

import com.carddemo.refdata.entity.TransactionType;
import com.carddemo.refdata.service.TransactionTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reference/types")
public class TransactionTypeController {

    private final TransactionTypeService service;

    public TransactionTypeController(TransactionTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<TransactionType> list() {
        return service.findAll();
    }

    @GetMapping("/{code}")
    public ResponseEntity<TransactionType> get(@PathVariable String code) {
        TransactionType entity = service.findByCode(code);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entity);
    }

    @PostMapping
    public ResponseEntity<TransactionType> create(@Valid @RequestBody TransactionType entity) {
        TransactionType created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{code}")
    public ResponseEntity<TransactionType> update(@PathVariable String code,
                                                  @Valid @RequestBody TransactionType entity) {
        TransactionType updated = service.update(code, entity);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        if (!service.delete(code)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
