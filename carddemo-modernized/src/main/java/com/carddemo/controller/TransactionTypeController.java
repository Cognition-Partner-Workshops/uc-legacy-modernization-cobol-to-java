package com.carddemo.controller;

import com.carddemo.model.TransactionCategory;
import com.carddemo.model.TransactionType;
import com.carddemo.service.TransactionTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Transaction Type Management controller - replaces COBOL programs:
 *   COTRTLIC.cbl (CTLI transaction) -> GET/DELETE /api/admin/transaction-types
 *   COTRTUPC.cbl (CTTU transaction) -> POST/PUT  /api/admin/transaction-types
 *
 * Legacy CICS flow (COTRTLIC):
 *   1. EXEC SQL DECLARE cursor FOR SELECT * FROM TRAN_TYPE_TABLE
 *   2. EXEC SQL OPEN cursor
 *   3. EXEC SQL FETCH loop to populate BMS map
 *   4. User selects 'U' (update) -> XCTL to COTRTUPC
 *   5. User selects 'D' (delete) -> EXEC SQL DELETE
 *
 * This controller uses synchronous JDBC since transaction types are
 * reference data stored in a relational database (DB2 replacement).
 * ODBC data sources can be accessed via JDBC-ODBC bridge configuration.
 *
 * Access: Admin users only
 */
@RestController
@RequestMapping("/api/admin/transaction-types")
public class TransactionTypeController {

    private final TransactionTypeService transactionTypeService;

    public TransactionTypeController(TransactionTypeService transactionTypeService) {
        this.transactionTypeService = transactionTypeService;
    }

    // Transaction Type endpoints

    @GetMapping
    public List<TransactionType> listTypes() {
        return transactionTypeService.listTypes();
    }

    @GetMapping("/{typeCode}")
    public TransactionType getType(@PathVariable String typeCode) {
        return transactionTypeService.getType(typeCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionType createType(@RequestBody TransactionType type) {
        return transactionTypeService.createType(type);
    }

    @PutMapping("/{typeCode}")
    public TransactionType updateType(@PathVariable String typeCode,
                                       @RequestBody TransactionType type) {
        return transactionTypeService.updateType(typeCode, type);
    }

    @DeleteMapping("/{typeCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteType(@PathVariable String typeCode) {
        transactionTypeService.deleteType(typeCode);
    }

    // Transaction Category endpoints

    @GetMapping("/categories")
    public List<TransactionCategory> listCategories(
            @RequestParam(required = false) String typeCode) {
        if (typeCode != null && !typeCode.isBlank()) {
            return transactionTypeService.listCategoriesByType(typeCode);
        }
        return transactionTypeService.listCategories();
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionCategory createCategory(@RequestBody TransactionCategory category) {
        return transactionTypeService.createCategory(category);
    }

    @DeleteMapping("/{typeCode}/categories/{categoryCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable String typeCode,
                                @PathVariable Integer categoryCode) {
        transactionTypeService.deleteCategory(typeCode, categoryCode);
    }
}
