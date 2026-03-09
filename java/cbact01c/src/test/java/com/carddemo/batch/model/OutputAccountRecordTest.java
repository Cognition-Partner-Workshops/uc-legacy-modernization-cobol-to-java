package com.carddemo.batch.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link OutputAccountRecord}, verifying the business rules
 * from COBOL paragraph 1300-POPUL-ACCT-RECORD.
 */
class OutputAccountRecordTest {

    @Test
    void zeroCycDebitDefaultsTo2525() {
        AccountRecord acct = new AccountRecord(
                1L, "Y",
                new BigDecimal("1940.00"),
                new BigDecimal("20200.00"),
                new BigDecimal("10200.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),  // zero → should become 2525.00
                "A000000000", "A000000000"
        );

        OutputAccountRecord out = OutputAccountRecord.fromAccountRecord(acct);

        assertEquals(new BigDecimal("2525.00"), out.currCycDebit(),
                "Zero cycle debit should default to 2525.00 per COBOL rule");
    }

    @Test
    void nonZeroCycDebitPreserved() {
        AccountRecord acct = new AccountRecord(
                2L, "Y",
                new BigDecimal("1580.00"),
                new BigDecimal("61300.00"),
                new BigDecimal("54480.00"),
                "2013-06-19", "2024-08-11", "2024-08-11",
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),  // non-zero → preserved
                "", "A000000000"
        );

        OutputAccountRecord out = OutputAccountRecord.fromAccountRecord(acct);

        assertEquals(new BigDecimal("500.00"), out.currCycDebit(),
                "Non-zero cycle debit should be preserved");
    }

    @Test
    void reissueDateReformatted() {
        AccountRecord acct = new AccountRecord(
                1L, "Y",
                new BigDecimal("1940.00"),
                new BigDecimal("20200.00"),
                new BigDecimal("10200.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "", "A000000000"
        );

        OutputAccountRecord out = OutputAccountRecord.fromAccountRecord(acct);

        assertEquals("20250520", out.reissueDate(),
                "Reissue date should be reformatted from YYYY-MM-DD to YYYYMMDD");
    }

    @Test
    void toFixedWidthContainsAllFields() {
        AccountRecord acct = new AccountRecord(
                1L, "Y",
                new BigDecimal("1940.00"),
                new BigDecimal("20200.00"),
                new BigDecimal("10200.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "", "A000000000"
        );

        OutputAccountRecord out = OutputAccountRecord.fromAccountRecord(acct);
        String fw = out.toFixedWidth();

        // Verify the account ID is at the start
        assertTrue(fw.startsWith("00000000001"), "Should start with account ID");
        // Verify active status follows
        assertEquals('Y', fw.charAt(11));
        // Verify group ID is present
        assertTrue(fw.contains("A000000000"), "Should contain group ID");
    }

    @Test
    void toFixedWidthOverpunchEncoding() {
        AccountRecord acct = new AccountRecord(
                5L, "Y",
                new BigDecimal("3450.00"),
                new BigDecimal("38190.00"),
                new BigDecimal("24300.00"),
                "2012-10-03", "2025-03-09", "2025-03-09",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "", "A000000000"
        );

        OutputAccountRecord out = OutputAccountRecord.fromAccountRecord(acct);
        String fw = out.toFixedWidth();

        // Account ID: 00000000005
        assertEquals("00000000005", fw.substring(0, 11));

        // ACCT-CURR-BAL = 3450.00 → unscaled 345000 → 12 digits: 000000345000
        // Last digit 0 → overpunch '{' → "00000034500{"
        // Starts at position 12, 12 chars
        String currBal = fw.substring(12, 24);
        assertEquals("00000034500{", currBal,
                "Current balance should be overpunch-encoded");
    }
}
