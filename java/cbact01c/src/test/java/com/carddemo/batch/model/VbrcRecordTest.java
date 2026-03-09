package com.carddemo.batch.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link VbrcRecord1} and {@link VbrcRecord2}, verifying the
 * business rules from COBOL paragraph 1500-POPUL-VBRC-RECORD.
 */
class VbrcRecordTest {

    private final AccountRecord sampleAcct = new AccountRecord(
            1L, "Y",
            new BigDecimal("1940.00"),
            new BigDecimal("20200.00"),
            new BigDecimal("10200.00"),
            "2014-11-20", "2025-05-20", "2025-05-20",
            BigDecimal.ZERO, BigDecimal.ZERO,
            "", "A000000000"
    );

    // ---- VbrcRecord1 tests ----

    @Test
    void vb1FromAccountRecord() {
        VbrcRecord1 vb1 = VbrcRecord1.fromAccountRecord(sampleAcct);

        assertEquals(1L, vb1.acctId());
        assertEquals("Y", vb1.activeStatus());
    }

    @Test
    void vb1FixedWidthLength() {
        VbrcRecord1 vb1 = VbrcRecord1.fromAccountRecord(sampleAcct);
        String fw = vb1.toFixedWidth();

        assertEquals(VbrcRecord1.RECORD_LENGTH, fw.length(),
                "VB1 record should be exactly 12 characters");
        assertEquals("00000000001Y", fw);
    }

    // ---- VbrcRecord2 tests ----

    @Test
    void vb2FromAccountRecord() {
        VbrcRecord2 vb2 = VbrcRecord2.fromAccountRecord(sampleAcct);

        assertEquals(1L, vb2.acctId());
        assertEquals(new BigDecimal("1940.00"), vb2.currBal());
        assertEquals(new BigDecimal("20200.00"), vb2.creditLimit());
        assertEquals("2025", vb2.reissueYear(),
                "Reissue year should be first 4 chars of YYYY-MM-DD date");
    }

    @Test
    void vb2FixedWidthLength() {
        VbrcRecord2 vb2 = VbrcRecord2.fromAccountRecord(sampleAcct);
        String fw = vb2.toFixedWidth();

        assertEquals(VbrcRecord2.RECORD_LENGTH, fw.length(),
                "VB2 record should be exactly 39 characters");
    }

    @Test
    void vb2FixedWidthContent() {
        VbrcRecord2 vb2 = VbrcRecord2.fromAccountRecord(sampleAcct);
        String fw = vb2.toFixedWidth();

        // Account ID
        assertEquals("00000000001", fw.substring(0, 11));
        // Reissue year at the end
        assertEquals("2025", fw.substring(35, 39));
    }

    @Test
    void vb2DifferentReissueYear() {
        AccountRecord acct2 = new AccountRecord(
                10L, "Y",
                new BigDecimal("1590.00"),
                new BigDecimal("54010.00"),
                new BigDecimal("44420.00"),
                "2015-09-13", "2023-01-27", "2023-01-27",
                BigDecimal.ZERO, BigDecimal.ZERO,
                "", "A000000000"
        );

        VbrcRecord2 vb2 = VbrcRecord2.fromAccountRecord(acct2);
        assertEquals("2023", vb2.reissueYear());
    }
}
