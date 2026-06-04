package com.carddemo.reference.controller;

import com.carddemo.reference.dto.BatchOperationDto;
import com.carddemo.reference.dto.TransactionTypeDto;
import com.carddemo.reference.service.TransactionTypeService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for transaction types, replacing the CICS online programs
 * {@code COTRTLIC} (list/update/delete) and {@code COTRTUPC} (add/edit), plus
 * the batch maintenance program {@code COBTUPDT}.
 */
@RestController
@RequestMapping("/api/transaction-types")
public class TransactionTypeController {

    private final TransactionTypeService service;

    public TransactionTypeController(TransactionTypeService service) {
        this.service = service;
    }

    /** GET /api/transaction-types &ndash; paginated list with optional search. */
    @GetMapping
    public Page<TransactionTypeDto> list(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return service.listTypes(search, pageable);
    }

    /** GET /api/transaction-types/{code} &ndash; single type with categories. */
    @GetMapping("/{code}")
    public TransactionTypeDto get(@PathVariable String code) {
        return service.getType(code);
    }

    /** POST /api/transaction-types &ndash; create a new type. */
    @PostMapping
    public ResponseEntity<TransactionTypeDto> create(
            @Valid @RequestBody TransactionTypeDto dto) {
        TransactionTypeDto created = service.createType(dto);
        return ResponseEntity
                .created(URI.create("/api/transaction-types/" + created.getTypeCode()))
                .body(created);
    }

    /** PUT /api/transaction-types/{code} &ndash; update the description. */
    @PutMapping("/{code}")
    public TransactionTypeDto update(
            @PathVariable String code,
            @Valid @RequestBody TransactionTypeDto dto) {
        return service.updateType(code, dto);
    }

    /**
     * DELETE /api/transaction-types/{code} &ndash; delete a type. Returns 409
     * Conflict when categories exist (ON DELETE RESTRICT).
     */
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        service.deleteType(code);
    }

    /**
     * POST /api/transaction-types/batch &ndash; apply a list of
     * INSERT/UPDATE/DELETE operations transactionally (replaces COBTUPDT).
     */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void batch(@Valid @RequestBody List<@Valid BatchOperationDto> operations) {
        service.processBatch(operations);
    }
}
