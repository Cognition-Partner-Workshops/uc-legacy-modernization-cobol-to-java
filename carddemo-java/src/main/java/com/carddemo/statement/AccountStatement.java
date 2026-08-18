package com.carddemo.statement;

import java.math.BigDecimal;
import java.util.List;

/**
 * One statement as produced by {@code 5000-CREATE-STATEMENT} plus its transaction
 * and trailer lines: the 80-column STMTFILE records and 100-column HTMLFILE records.
 */
public record AccountStatement(
        String accountId,
        String customerName,
        int transactionCount,
        BigDecimal totalAmount,
        List<String> textRecords,
        List<String> htmlRecords) {

    public static final int TEXT_RECORD_LENGTH = 80;
    public static final int HTML_RECORD_LENGTH = 100;
}
