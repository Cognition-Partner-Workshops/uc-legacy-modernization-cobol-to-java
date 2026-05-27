package com.cardemo.batch.io;

import com.cardemo.batch.model.AccountRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CobolDataParserTest {

    @ParameterizedTest
    @CsvSource({
            "'00000001940{', 2,  194.00",
            "'00000020200{', 2,  2020.00",
            "'00000010200{', 2,  1020.00",
            "'00000000000{', 2,  0.00",
            "'0000000100A',  2,  10.01",
            "'0000000100J',  2,  -10.01",
            "'00000000000}', 2,  0.00",
    })
    void parseSignedDecimal(String raw, int scale, String expected) {
        BigDecimal result = CobolDataParser.parseSignedDecimal(raw, scale);
        assertEquals(new BigDecimal(expected), result);
    }

    @ParameterizedTest
    @CsvSource({
            "194.00,    12, 2, '00000001940{'",
            "2020.00,   12, 2, '00000020200{'",
            "-10.01,    12, 2, '00000000100J'",
            "0.00,      12, 2, '00000000000{'",
            "2525.00,   12, 2, '00000025250{'",
            "1005.00,   12, 2, '00000010050{'",
            "-1025.00,  12, 2, '00000010250}'",
            "-2500.00,  12, 2, '00000025000}'",
    })
    void formatSignedDecimal(String value, int totalDigits, int scale, String expected) {
        String result = CobolDataParser.formatSignedDecimal(new BigDecimal(value), totalDigits, scale);
        assertEquals(expected, result);
    }

    @Test
    void roundTripSignedDecimal() {
        BigDecimal original = new BigDecimal("194.00");
        String formatted = CobolDataParser.formatSignedDecimal(original, 12, 2);
        BigDecimal parsed = CobolDataParser.parseSignedDecimal(formatted, 2);
        assertEquals(0, original.compareTo(parsed));
    }

    @Test
    void parseFirstAccountRecord() {
        // First line from the real acctdata.txt (padded to 300 chars)
        String line = "00000000001Y00000001940{00000020200{00000010200{"
                + "2014-11-20" + "2025-05-20" + "2025-05-20"
                + "00000000000{" + "00000000000{"
                + "A000000000" + "          "
                + " ".repeat(178);

        AccountRecord r = CobolDataParser.parseAccountRecord(line);

        assertEquals(1L, r.acctId());
        assertEquals('Y', r.activeStatus());
        assertEquals(new BigDecimal("194.00"), r.currBal());
        assertEquals(new BigDecimal("2020.00"), r.creditLimit());
        assertEquals(new BigDecimal("1020.00"), r.cashCreditLimit());
        assertEquals("2014-11-20", r.openDate());
        assertEquals("2025-05-20", r.expirationDate());
        assertEquals("2025-05-20", r.reissueDate());
        assertEquals(new BigDecimal("0.00"), r.currCycCredit());
        assertEquals(new BigDecimal("0.00"), r.currCycDebit());
        assertEquals("A000000000", r.addrZip());
        assertEquals("", r.groupId());
    }

    @Test
    void parseSignedDecimalThrowsOnBadChar() {
        assertThrows(IllegalArgumentException.class,
                () -> CobolDataParser.parseSignedDecimal("0000000000Z", 2));
    }
}
