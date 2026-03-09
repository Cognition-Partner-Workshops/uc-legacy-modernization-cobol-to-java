package com.carddemo.batch.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ArrayRecord}, verifying the business rules from
 * COBOL paragraph 1400-POPUL-ARRAY-RECORD.
 */
class ArrayRecordTest {

    @Test
    void slotPopulationRules() {
        AccountRecord acct = new AccountRecord(
                1L, "Y",
                new BigDecimal("1940.00"),
                new BigDecimal("20200.00"),
                new BigDecimal("10200.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "", "A000000000"
        );

        ArrayRecord arr = ArrayRecord.fromAccountRecord(acct);

        // Slot 1: balance = account balance, debit = 1005.00
        assertEquals(new BigDecimal("1940.00"), arr.balances()[0]);
        assertEquals(new BigDecimal("1005.00"), arr.cycDebits()[0]);

        // Slot 2: balance = account balance, debit = 1525.00
        assertEquals(new BigDecimal("1940.00"), arr.balances()[1]);
        assertEquals(new BigDecimal("1525.00"), arr.cycDebits()[1]);

        // Slot 3: balance = -1025.00, debit = -2500.00
        assertEquals(new BigDecimal("-1025.00"), arr.balances()[2]);
        assertEquals(new BigDecimal("-2500.00"), arr.cycDebits()[2]);

        // Slots 4 and 5: zeroes (from INITIALIZE)
        assertEquals(BigDecimal.ZERO, arr.balances()[3]);
        assertEquals(BigDecimal.ZERO, arr.cycDebits()[3]);
        assertEquals(BigDecimal.ZERO, arr.balances()[4]);
        assertEquals(BigDecimal.ZERO, arr.cycDebits()[4]);
    }

    @Test
    void accountIdPreserved() {
        AccountRecord acct = new AccountRecord(
                42L, "Y",
                new BigDecimal("3020.00"),
                new BigDecimal("65630.00"),
                new BigDecimal("51030.00"),
                "2016-09-19", "2025-09-19", "2025-09-19",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "", "A000000000"
        );

        ArrayRecord arr = ArrayRecord.fromAccountRecord(acct);
        assertEquals(42L, arr.acctId());
    }

    @Test
    void toFixedWidthStartsWithAccountId() {
        AccountRecord acct = new AccountRecord(
                7L, "Y",
                new BigDecimal("1930.00"),
                new BigDecimal("20650.00"),
                new BigDecimal("2640.00"),
                "2012-10-12", "2024-12-13", "2024-12-13",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "", "A000000000"
        );

        ArrayRecord arr = ArrayRecord.fromAccountRecord(acct);
        String fw = arr.toFixedWidth();

        assertTrue(fw.startsWith("00000000007"), "Should start with account ID");
        // Should end with 4-char filler
        assertTrue(fw.endsWith("    "), "Should end with 4-space filler");
    }

    @Test
    void toFixedWidthNegativeValuesEncoded() {
        AccountRecord acct = new AccountRecord(
                1L, "Y",
                new BigDecimal("1940.00"),
                new BigDecimal("20200.00"),
                new BigDecimal("10200.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "", "A000000000"
        );

        ArrayRecord arr = ArrayRecord.fromAccountRecord(acct);
        String fw = arr.toFixedWidth();

        // Slot 3 balance = -1025.00 → overpunch for negative zero = '}'
        // Slot 3 debit = -2500.00 → overpunch for negative zero = '}'
        // The string should contain '}' characters for negative values ending in 0
        assertTrue(fw.contains("}"), "Should contain overpunch for negative values");
    }
}
