package com.carddemo.reference.controller;

import com.carddemo.reference.dto.TransactionCategoryDto;
import com.carddemo.reference.service.TransactionTypeService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for transaction categories nested under a transaction type.
 *
 * <p>Backs the transaction category reference data described by
 * {@code app/cpy/CVTRA04Y.cpy} / DB2 table
 * {@code CARDDEMO.TRANSACTION_TYPE_CATEGORY}.</p>
 */
@RestController
@RequestMapping("/api/transaction-types/{code}/categories")
public class TransactionCategoryController {

    private final TransactionTypeService service;

    public TransactionCategoryController(TransactionTypeService service) {
        this.service = service;
    }

    /** GET &ndash; list categories for a type. */
    @GetMapping
    public List<TransactionCategoryDto> list(@PathVariable String code) {
        return service.listCategories(code);
    }

    /** POST &ndash; create a category for a type. */
    @PostMapping
    public ResponseEntity<TransactionCategoryDto> create(
            @PathVariable String code,
            @Valid @RequestBody TransactionCategoryDto dto) {
        TransactionCategoryDto created = service.createCategory(code, dto);
        return ResponseEntity
                .created(URI.create("/api/transaction-types/" + code + "/categories/"
                        + created.getCategoryCode()))
                .body(created);
    }

    /** PUT &ndash; update a category description. */
    @PutMapping("/{catCode}")
    public TransactionCategoryDto update(
            @PathVariable String code,
            @PathVariable String catCode,
            @Valid @RequestBody TransactionCategoryDto dto) {
        return service.updateCategory(code, catCode, dto);
    }

    /** DELETE &ndash; remove a category. */
    @DeleteMapping("/{catCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code, @PathVariable String catCode) {
        service.deleteCategory(code, catCode);
    }
}
