package com.carddemo.repository.jdbc;

import com.carddemo.model.TransactionType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JDBC repository for Transaction Type reference data.
 *
 * Replaces legacy DB2 operations in COBOL programs:
 *   EXEC SQL SELECT ... FROM TRAN_TYPE_TABLE -> findAll(), findByTypeCode()
 *   EXEC SQL INSERT INTO TRAN_TYPE_TABLE     -> save()
 *   EXEC SQL UPDATE TRAN_TYPE_TABLE          -> update()
 *   EXEC SQL DELETE FROM TRAN_TYPE_TABLE     -> deleteByTypeCode()
 *
 * Programs modernized: COTRTLIC (Tran Type List), COTRTUPC (Tran Type Add/Edit),
 *                       COBTUPDT (Batch Maintain Transaction Types)
 *
 * This layer demonstrates JDBC/ODBC connectivity for relational data,
 * suitable for reference data that benefits from relational constraints.
 * In production, the H2 driver can be swapped for PostgreSQL or any
 * JDBC-compliant driver, including ODBC via JDBC-ODBC bridge.
 */
@Repository
public class TransactionTypeJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<TransactionType> ROW_MAPPER = (rs, rowNum) ->
            new TransactionType(
                    rs.getString("type_code"),
                    rs.getString("type_description")
            );

    public TransactionTypeJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TransactionType> findAll() {
        return jdbcTemplate.query("SELECT type_code, type_description FROM transaction_types ORDER BY type_code", ROW_MAPPER);
    }

    public Optional<TransactionType> findByTypeCode(String typeCode) {
        List<TransactionType> results = jdbcTemplate.query(
                "SELECT type_code, type_description FROM transaction_types WHERE type_code = ?",
                ROW_MAPPER, typeCode);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public TransactionType save(TransactionType transactionType) {
        jdbcTemplate.update(
                "MERGE INTO transaction_types (type_code, type_description) KEY(type_code) VALUES (?, ?)",
                transactionType.getTypeCode(), transactionType.getTypeDescription());
        return transactionType;
    }

    public int deleteByTypeCode(String typeCode) {
        return jdbcTemplate.update("DELETE FROM transaction_types WHERE type_code = ?", typeCode);
    }
}
