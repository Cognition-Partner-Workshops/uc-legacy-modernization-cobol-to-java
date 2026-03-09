package com.carddemo.batch.util;

import com.carddemo.batch.model.AccountRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CobolDataParserTest {

    // ---- Overpunch decoding ----

    @Nested
    @DisplayName("parseSignedDecimal — overpunch decoding")
    class OverpunchTests {

        @ParameterizedTest(name = "\"{0}\" (scale {1}) → {2}")
        @CsvSource({
                // Positive zero overpunch '{'
                "00000001940{, 2, 194.00",
                "00000020200{, 2, 2020.00",
                "00000010200{, 2, 1020.00",
                "00000000000{, 2, 0.00",
                // Positive digit overpunches A-I (12 chars for PIC S9(10)V99)
                "00000001940A, 2, 194.01",
                "00000001940I, 2, 194.09",
                // Negative overpunches
                "00000001940}, 2, -194.00",
                "00000001940J, 2, -194.01",
                "00000001940R, 2, -194.09",
                // Plain digits (no overpunch)
                "000000019400, 2, 194.00",
        })
        void shouldDecodeOverpunchCorrectly(String raw, int scale, String expected) {
            BigDecimal result = CobolDataParser.parseSignedDecimal(raw, scale);
            assertEquals(new BigDecimal(expected), result);
        }

        @Test
        @DisplayName("null or empty returns ZERO")
        void nullOrEmptyReturnsZero() {
            assertEquals(BigDecimal.ZERO, CobolDataParser.parseSignedDecimal(null, 2));
            assertEquals(BigDecimal.ZERO, CobolDataParser.parseSignedDecimal("", 2));
        }

        @Test
        @DisplayName("invalid overpunch character throws")
        void invalidOverpunchThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> CobolDataParser.parseSignedDecimal("0000000194Z", 2));
        }
    }

    // ---- Full record parsing ----

    @Nested
    @DisplayName("parseAccountRecord — full record parsing")
    class RecordParsingTests {

        /** First line from acctdata.txt (padded to 300 chars). */
        private static final String LINE_1 =
                "00000000001Y00000001940{00000020200{00000010200{" +
                "2014-11-202025-05-202025-05-20" +
                "00000000000{00000000000{" +
                "A000000000";  // 10 chars (addr-zip) — group-id and filler are spaces

        @Test
        @DisplayName("parses account ID correctly")
        void parsesAcctId() {
            AccountRecord rec = CobolDataParser.parseAccountRecord(LINE_1);
            assertEquals(1L, rec.acctId());
        }

        @Test
        @DisplayName("parses active status")
        void parsesActiveStatus() {
            AccountRecord rec = CobolDataParser.parseAccountRecord(LINE_1);
            assertEquals("Y", rec.acctActiveStatus());
        }

        @Test
        @DisplayName("parses monetary fields with overpunch")
        void parsesMonetaryFields() {
            AccountRecord rec = CobolDataParser.parseAccountRecord(LINE_1);
            assertEquals(new BigDecimal("194.00"), rec.acctCurrBal());
            assertEquals(new BigDecimal("2020.00"), rec.acctCreditLimit());
            assertEquals(new BigDecimal("1020.00"), rec.acctCashCreditLimit());
        }

        @Test
        @DisplayName("parses date fields")
        void parsesDateFields() {
            AccountRecord rec = CobolDataParser.parseAccountRecord(LINE_1);
            assertEquals("2014-11-20", rec.acctOpenDate());
            assertEquals("2025-05-20", rec.acctExpiraionDate());
            assertEquals("2025-05-20", rec.acctReissueDate());
        }

        @Test
        @DisplayName("parses cycle credit and debit")
        void parsesCycleFields() {
            AccountRecord rec = CobolDataParser.parseAccountRecord(LINE_1);
            assertEquals(new BigDecimal("0.00"), rec.acctCurrCycCredit());
            assertEquals(new BigDecimal("0.00"), rec.acctCurrCycDebit());
        }

        @Test
        @DisplayName("parses second record from sample data")
        void parsesSecondRecord() {
            String line2 =
                    "00000000002Y00000001580{00000061300{00000054480{" +
                    "2013-06-192024-08-112024-08-11" +
                    "00000000000{00000000000{" +
                    "A000000000";
            AccountRecord rec = CobolDataParser.parseAccountRecord(line2);
            assertEquals(2L, rec.acctId());
            assertEquals("Y", rec.acctActiveStatus());
            assertEquals(new BigDecimal("158.00"), rec.acctCurrBal());
            assertEquals(new BigDecimal("6130.00"), rec.acctCreditLimit());
            assertEquals(new BigDecimal("5448.00"), rec.acctCashCreditLimit());
            assertEquals("2013-06-19", rec.acctOpenDate());
            assertEquals("2024-08-11", rec.acctExpiraionDate());
            assertEquals("2024-08-11", rec.acctReissueDate());
        }
    }
}
