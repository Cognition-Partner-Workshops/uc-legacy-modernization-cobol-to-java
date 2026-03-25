package com.carddemo.batch.io;

import com.carddemo.batch.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AccountFileWriter} — output formatting.
 *
 * Verifies that the pipe-delimited output format preserves all field values
 * and is deterministic (identical inputs → identical outputs).
 */
class AccountFileWriterTest {

    @Test
    @DisplayName("Format OUT-ACCT-REC with all fields")
    void shouldFormatOutRecord() {
        var rec = new OutAccountRecord(
                1L, "Y",
                new BigDecimal("194.00"),
                new BigDecimal("2020.00"),
                new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "20250520",
                new BigDecimal("0.00"),
                new BigDecimal("2525.00"),
                "A000000000"
        );

        String formatted = AccountFileWriter.formatOutRecord(rec);
        String[] fields = formatted.split("\\|", -1);

        assertEquals(11, fields.length);
        assertEquals("00000000001", fields[0]);
        assertEquals("Y", fields[1]);
        assertEquals("194.00", fields[2]);
        assertEquals("2020.00", fields[3]);
        assertEquals("1020.00", fields[4]);
        assertEquals("2014-11-20", fields[5]);
        assertEquals("2025-05-20", fields[6]);
        assertEquals("20250520", fields[7]);
        assertEquals("0.00", fields[8]);
        assertEquals("2525.00", fields[9]);
        assertEquals("A000000000", fields[10]);
    }

    @Test
    @DisplayName("Format ARR-ARRAY-REC with 5 balance entries")
    void shouldFormatArrayRecord() {
        var entries = List.of(
                new ArrayRecord.BalanceEntry(new BigDecimal("194.00"), new BigDecimal("1005.00")),
                new ArrayRecord.BalanceEntry(new BigDecimal("194.00"), new BigDecimal("1525.00")),
                new ArrayRecord.BalanceEntry(new BigDecimal("-1025.00"), new BigDecimal("-2500.00")),
                ArrayRecord.BalanceEntry.ZERO,
                ArrayRecord.BalanceEntry.ZERO
        );
        var rec = new ArrayRecord(1L, entries);

        String formatted = AccountFileWriter.formatArrayRecord(rec);

        assertTrue(formatted.startsWith("00000000001|"));
        assertTrue(formatted.contains("194.00|1005.00"));
        assertTrue(formatted.contains("194.00|1525.00"));
        assertTrue(formatted.contains("-1025.00|-2500.00"));
        assertTrue(formatted.contains("0|0")); // zero entries
    }

    @Test
    @DisplayName("Format VBR record 1 (short)")
    void shouldFormatVbrRecord1() {
        var rec = new VbrRecord1(1L, "Y");
        String formatted = AccountFileWriter.formatVbrRecord1(rec);
        assertEquals("00000000001Y", formatted);
    }

    @Test
    @DisplayName("Format VBR record 2 (long)")
    void shouldFormatVbrRecord2() {
        var rec = new VbrRecord2(1L, new BigDecimal("194.00"),
                new BigDecimal("2020.00"), "2025");
        String formatted = AccountFileWriter.formatVbrRecord2(rec);

        String[] fields = formatted.split("\\|", -1);
        assertEquals(4, fields.length);
        assertEquals("00000000001", fields[0]);
        assertEquals("194.00", fields[1]);
        assertEquals("2020.00", fields[2]);
        assertEquals("2025", fields[3]);
    }
}
