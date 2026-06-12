package com.carddemo.refdata.controller;

import java.util.List;

import com.carddemo.refdata.entity.TransactionCategory;
import com.carddemo.refdata.service.TransactionCategoryService;
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
@RequestMapping("/reference/categories")
public class TransactionCategoryController {

    private final TransactionCategoryService service;

    public TransactionCategoryController(TransactionCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<TransactionCategory> list() {
        return service.findAll();
    }

    @GetMapping("/{typeCode}/{catCode}")
    public ResponseEntity<TransactionCategory> get(@PathVariable String typeCode,
                                                   @PathVariable int catCode) {
        TransactionCategory entity = service.findByKey(typeCode, catCode);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entity);
    }

    @PostMapping
    public ResponseEntity<TransactionCategory> create(@Valid @RequestBody TransactionCategory entity) {
        TransactionCategory created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{typeCode}/{catCode}")
    public ResponseEntity<TransactionCategory> update(@PathVariable String typeCode,
                                                      @PathVariable int catCode,
                                                      @Valid @RequestBody TransactionCategory entity) {
        TransactionCategory updated = service.update(typeCode, catCode, entity);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{typeCode}/{catCode}")
    public ResponseEntity<Void> delete(@PathVariable String typeCode,
                                       @PathVariable int catCode) {
        if (!service.delete(typeCode, catCode)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
