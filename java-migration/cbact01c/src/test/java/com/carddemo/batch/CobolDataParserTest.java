package com.carddemo.batch;

import com.carddemo.batch.io.CobolDataParser;
import com.carddemo.batch.model.AccountRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CobolDataParser} — verifies that fixed-width COBOL
 * record parsing and EBCDIC overpunch decoding produce values identical
 * to the original COBOL program's interpretation.
 */
class CobolDataParserTest {

    // ── Overpunch decoding ──────────────────────────────────────────

    @ParameterizedTest(name = "parseSignedDecimal(\"{0}\", {1}) = {2}")
    @CsvSource({
            // Positive overpunch: '{' = +0, implied V99 decimal
            "00000001940{, 2, 194.00",
            "00000020200{, 2, 2020.00",
            "00000010200{, 2, 1020.00",
            "00000000000{, 2, 0.00",
            // Positive overpunch with last digit non-zero: 'A' = +1
            "00000000150A, 2, 15.01",
            // All zeros with positive overpunch
            "00000000000{, 2, 0.00",
            // No decimal places
            "00000001940{, 0, 19400",
    })
    @DisplayName("Signed zoned decimal with positive overpunch")
    void testPositiveOverpunch(String raw, int decimals, String expected) {
        BigDecimal result = CobolDataParser.parseSignedDecimal(raw, decimals);
        assertEquals(new BigDecimal(expected), result);
    }

    @ParameterizedTest(name = "parseSignedDecimal(\"{0}\", {1}) = {2}")
    @CsvSource({
            // Negative overpunch: '}' = -0, 'J' = -1, 'R' = -9
            "0000000100}, 2, -10.00",
            "0000000025J, 2, -2.51",
            "000000000}  , 0, 0",
    })
    @DisplayName("Signed zoned decimal with negative overpunch")
    void testNegativeOverpunch(String raw, int decimals, String expected) {
        BigDecimal result = CobolDataParser.parseSignedDecimal(raw.strip(), decimals);
        assertEquals(new BigDecimal(expected), result);
    }

    @Test
    @DisplayName("Null and blank inputs return zero")
    void testNullAndBlankReturnsZero() {
        assertEquals(BigDecimal.ZERO, CobolDataParser.parseSignedDecimal(null, 2));
        assertEquals(BigDecimal.ZERO, CobolDataParser.parseSignedDecimal("", 2));
        assertEquals(BigDecimal.ZERO, CobolDataParser.parseSignedDecimal("   ", 2));
    }

    // ── Full record parsing ─────────────────────────────────────────

    @Test
    @DisplayName("Parse first record from acctdata.txt — account 1")
    void testParseFirstRecord() {
        // First line of acctdata.txt (padded to 300 chars in the file)
        String line = "00000000001Y00000001940{00000020200{00000010200{"
                + "2014-11-202025-05-202025-05-2000000000000{00000000000{"
                + "A000000000";

        AccountRecord rec = CobolDataParser.parseLine(line);

        assertEquals(1L, rec.acctId());
        assertEquals("Y", rec.activeStatus());
        assertEquals(new BigDecimal("194.00"), rec.currBal());
        assertEquals(new BigDecimal("2020.00"), rec.creditLimit());
        assertEquals(new BigDecimal("1020.00"), rec.cashCreditLimit());
        assertEquals("2014-11-20", rec.openDate());
        assertEquals("2025-05-20", rec.expirationDate());
        assertEquals("2025-05-20", rec.reissueDate());
        assertEquals(new BigDecimal("0.00"), rec.currCycCredit());
        assertEquals(new BigDecimal("0.00"), rec.currCycDebit());
        assertEquals("A000000000", rec.addrZip());
        // GROUP-ID at offset 112 is spaces in the short test string
        assertEquals("", rec.groupId());
    }

    @Test
    @DisplayName("Parse second record from acctdata.txt — account 2")
    void testParseSecondRecord() {
        String line = "00000000002Y00000001580{00000061300{00000054480{"
                + "2013-06-192024-08-112024-08-1100000000000{00000000000{"
                + "A000000000";

        AccountRecord rec = CobolDataParser.parseLine(line);

        assertEquals(2L, rec.acctId());
        assertEquals("Y", rec.activeStatus());
        assertEquals(new BigDecimal("158.00"), rec.currBal());
        assertEquals(new BigDecimal("6130.00"), rec.creditLimit());
        assertEquals(new BigDecimal("5448.00"), rec.cashCreditLimit());
        assertEquals("2013-06-19", rec.openDate());
        assertEquals("2024-08-11", rec.expirationDate());
        assertEquals("2024-08-11", rec.reissueDate());
    }

    @Test
    @DisplayName("Parse record with large balance — account 16")
    void testParseLargeBalance() {
        String line = "00000000016Y00000007330{00000089220{00000026320{"
                + "2014-09-112024-01-252024-01-2500000000000{00000000000{"
                + "A000000000";

        AccountRecord rec = CobolDataParser.parseLine(line);

        assertEquals(16L, rec.acctId());
        assertEquals(new BigDecimal("733.00"), rec.currBal());
        assertEquals(new BigDecimal("8922.00"), rec.creditLimit());
        assertEquals(new BigDecimal("2632.00"), rec.cashCreditLimit());
    }

    @Test
    @DisplayName("Parse record with smallest balance — account 30")
    void testParseSmallBalance() {
        String line = "00000000030Y00000000020{00000001200{00000000930{"
                + "2011-08-262024-06-272024-06-2700000000000{00000000000{"
                + "A000000000";

        AccountRecord rec = CobolDataParser.parseLine(line);

        assertEquals(30L, rec.acctId());
        assertEquals(new BigDecimal("2.00"), rec.currBal());
        assertEquals(new BigDecimal("120.00"), rec.creditLimit());
        assertEquals(new BigDecimal("93.00"), rec.cashCreditLimit());
    }
}
