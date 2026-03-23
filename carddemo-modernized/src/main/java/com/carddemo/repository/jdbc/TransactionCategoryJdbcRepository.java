package com.carddemo.repository.jdbc;

import com.carddemo.model.TransactionCategory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JDBC repository for Transaction Category reference data.
 *
 * Replaces legacy DB2 operations for transaction category management.
 * Legacy COBOL: DSNTIAUL extracts (TRANEXTR JCL), DB2 cursor-based reads.
 *
 * Demonstrates JDBC connectivity for relational reference data that
 * requires foreign key constraints (category -> type).
 * Compatible with ODBC data sources via JDBC-ODBC bridge driver.
 */
@Repository
public class TransactionCategoryJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<TransactionCategory> ROW_MAPPER = (rs, rowNum) ->
            new TransactionCategory(
                    rs.getString("type_code"),
                    rs.getInt("category_code"),
                    rs.getString("category_description")
            );

    public TransactionCategoryJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TransactionCategory> findAll() {
        return jdbcTemplate.query(
                "SELECT type_code, category_code, category_description FROM transaction_categories ORDER BY type_code, category_code",
                ROW_MAPPER);
    }

    public List<TransactionCategory> findByTypeCode(String typeCode) {
        return jdbcTemplate.query(
                "SELECT type_code, category_code, category_description FROM transaction_categories WHERE type_code = ?",
                ROW_MAPPER, typeCode);
    }

    public Optional<TransactionCategory> findByTypeCodeAndCategoryCode(String typeCode, Integer categoryCode) {
        List<TransactionCategory> results = jdbcTemplate.query(
                "SELECT type_code, category_code, category_description FROM transaction_categories WHERE type_code = ? AND category_code = ?",
                ROW_MAPPER, typeCode, categoryCode);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public TransactionCategory save(TransactionCategory category) {
        jdbcTemplate.update(
                "MERGE INTO transaction_categories (type_code, category_code, category_description) KEY(type_code, category_code) VALUES (?, ?, ?)",
                category.getTypeCode(), category.getCategoryCode(), category.getCategoryDescription());
        return category;
    }

    public int deleteByTypeCodeAndCategoryCode(String typeCode, Integer categoryCode) {
        return jdbcTemplate.update(
                "DELETE FROM transaction_categories WHERE type_code = ? AND category_code = ?",
                typeCode, categoryCode);
    }
}
