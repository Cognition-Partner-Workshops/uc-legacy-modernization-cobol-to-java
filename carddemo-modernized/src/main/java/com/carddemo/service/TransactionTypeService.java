package com.carddemo.service;

import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.TransactionCategory;
import com.carddemo.model.TransactionType;
import com.carddemo.repository.jdbc.TransactionCategoryJdbcRepository;
import com.carddemo.repository.jdbc.TransactionTypeJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Transaction Type service - modernized business logic from COBOL programs:
 *
 *   COTRTLIC.cbl  -> listTypes(), deleteType()  (Tran Type List - CTLI transaction)
 *   COTRTUPC.cbl  -> createType(), updateType()  (Tran Type Add/Edit - CTTU transaction)
 *   COBTUPDT.cbl  -> batch transaction type maintenance
 *
 * Legacy flow (COTRTLIC):
 *   1. EXEC SQL DECLARE TRAN_TYPE_CURSOR CURSOR FOR SELECT ...
 *   2. EXEC SQL OPEN TRAN_TYPE_CURSOR
 *   3. EXEC SQL FETCH loop to populate screen list
 *   4. EXEC SQL DELETE FROM TRAN_TYPE_TABLE WHERE ...
 *
 * This service uses JDBC (synchronous) since transaction type data is
 * reference data stored in a relational DB (legacy DB2 replacement).
 * ODBC connectivity is supported via JDBC-ODBC bridge driver configuration.
 */
@Service
public class TransactionTypeService {

    private static final Logger log = LoggerFactory.getLogger(TransactionTypeService.class);

    private final TransactionTypeJdbcRepository typeRepository;
    private final TransactionCategoryJdbcRepository categoryRepository;

    public TransactionTypeService(TransactionTypeJdbcRepository typeRepository,
                                  TransactionCategoryJdbcRepository categoryRepository) {
        this.typeRepository = typeRepository;
        this.categoryRepository = categoryRepository;
    }

    // Transaction Type operations

    public List<TransactionType> listTypes() {
        return typeRepository.findAll();
    }

    public TransactionType getType(String typeCode) {
        return typeRepository.findByTypeCode(typeCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction type not found: " + typeCode));
    }

    public TransactionType createType(TransactionType type) {
        log.debug("Creating transaction type: {}", type.getTypeCode());
        return typeRepository.save(type);
    }

    public TransactionType updateType(String typeCode, TransactionType type) {
        log.debug("Updating transaction type: {}", typeCode);
        typeRepository.findByTypeCode(typeCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction type not found: " + typeCode));
        type.setTypeCode(typeCode);
        return typeRepository.save(type);
    }

    public void deleteType(String typeCode) {
        log.debug("Deleting transaction type: {}", typeCode);
        int deleted = typeRepository.deleteByTypeCode(typeCode);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Transaction type not found: " + typeCode);
        }
    }

    // Transaction Category operations

    public List<TransactionCategory> listCategories() {
        return categoryRepository.findAll();
    }

    public List<TransactionCategory> listCategoriesByType(String typeCode) {
        return categoryRepository.findByTypeCode(typeCode);
    }

    public TransactionCategory createCategory(TransactionCategory category) {
        log.debug("Creating transaction category: {}-{}", category.getTypeCode(), category.getCategoryCode());
        return categoryRepository.save(category);
    }

    public void deleteCategory(String typeCode, Integer categoryCode) {
        log.debug("Deleting transaction category: {}-{}", typeCode, categoryCode);
        int deleted = categoryRepository.deleteByTypeCodeAndCategoryCode(typeCode, categoryCode);
        if (deleted == 0) {
            throw new ResourceNotFoundException(
                    "Transaction category not found: " + typeCode + "-" + categoryCode);
        }
    }
}
