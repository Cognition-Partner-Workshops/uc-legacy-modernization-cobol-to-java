package com.carddemo.batch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CobolDecimalParser} — COBOL overpunch decimal parsing.
 *
 * Verifies that PIC S9(10)V99 fields with overpunch encoding are parsed
 * correctly. Uses values from the actual acctdata.txt sample file.
 */
class CobolDecimalParserTest {

    @Nested
    @DisplayName("Signed decimal with overpunch")
    class SignedDecimal {

        @Test
        @DisplayName("Positive value with '{' overpunch (digit 0, positive)")
        void shouldParsePositiveZeroOverpunch() {
            // "00000001940{" → 00000001940 + 0 = 19400 → /100 = 194.00
            var result = CobolDecimalParser.parseSignedDecimal("00000001940{", 2);
            assertEquals(new BigDecimal("194.00"), result);
        }

        @Test
        @DisplayName("Account 1 current balance: 00000001940{ = 194.00")
        void shouldParseAccount1Balance() {
            assertEquals(new BigDecimal("194.00"),
                    CobolDecimalParser.parseSignedDecimal("00000001940{", 2));
        }

        @Test
        @DisplayName("Account 1 credit limit: 00000020200{ = 2020.00")
        void shouldParseAccount1CreditLimit() {
            assertEquals(new BigDecimal("2020.00"),
                    CobolDecimalParser.parseSignedDecimal("00000020200{", 2));
        }

        @Test
        @DisplayName("Account 1 cash credit limit: 00000010200{ = 1020.00")
        void shouldParseAccount1CashCreditLimit() {
            assertEquals(new BigDecimal("1020.00"),
                    CobolDecimalParser.parseSignedDecimal("00000010200{", 2));
        }

        @Test
        @DisplayName("Zero value: 00000000000{ = 0.00")
        void shouldParseZeroValue() {
            assertEquals(new BigDecimal("0.00"),
                    CobolDecimalParser.parseSignedDecimal("00000000000{", 2));
        }

        @Test
        @DisplayName("Negative value with 'J' overpunch (digit 1, negative)")
        void shouldParseNegativeJ() {
            // "0000000100J" → 00000001001 → 10.01, negative
            var result = CobolDecimalParser.parseSignedDecimal("0000000100J", 2);
            assertEquals(new BigDecimal("-10.01"), result);
        }

        @Test
        @DisplayName("Negative value with '}' overpunch (digit 0, negative)")
        void shouldParseNegativeZeroOverpunch() {
            // "00000010000}" → 000000100000 → 1000.00, negative
            var result = CobolDecimalParser.parseSignedDecimal("00000010000}", 2);
            assertEquals(new BigDecimal("-1000.00"), result);
        }

        @Test
        @DisplayName("Positive 'A' overpunch (digit 1, positive)")
        void shouldParsePositiveA() {
            // "0000000250A" → 00000002501 → 25.01
            var result = CobolDecimalParser.parseSignedDecimal("0000000250A", 2);
            assertEquals(new BigDecimal("25.01"), result);
        }

        @Test
        @DisplayName("Positive 'I' overpunch (digit 9, positive)")
        void shouldParsePositiveI() {
            // "0000000250I" → 00000002509 → 25.09
            var result = CobolDecimalParser.parseSignedDecimal("0000000250I", 2);
            assertEquals(new BigDecimal("25.09"), result);
        }

        @Test
        @DisplayName("Negative 'R' overpunch (digit 9, negative)")
        void shouldParseNegativeR() {
            // "0000000250R" → 00000002509 → -25.09
            var result = CobolDecimalParser.parseSignedDecimal("0000000250R", 2);
            assertEquals(new BigDecimal("-25.09"), result);
        }
    }

    @Nested
    @DisplayName("Unsigned numeric")
    class UnsignedNumeric {

        @Test
        @DisplayName("Account ID: 00000000001 = 1")
        void shouldParseAccountId() {
            assertEquals(1L, CobolDecimalParser.parseUnsignedNumeric("00000000001"));
        }

        @Test
        @DisplayName("Larger account ID: 00000000050 = 50")
        void shouldParseLargerAccountId() {
            assertEquals(50L, CobolDecimalParser.parseUnsignedNumeric("00000000050"));
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Null input returns zero")
        void shouldHandleNull() {
            assertEquals(BigDecimal.ZERO, CobolDecimalParser.parseSignedDecimal(null, 2));
        }

        @Test
        @DisplayName("Blank input returns zero")
        void shouldHandleBlank() {
            assertEquals(BigDecimal.ZERO, CobolDecimalParser.parseSignedDecimal("   ", 2));
        }

        @Test
        @DisplayName("Null unsigned returns zero")
        void shouldHandleNullUnsigned() {
            assertEquals(0L, CobolDecimalParser.parseUnsignedNumeric(null));
        }

        @Test
        @DisplayName("Plain digit without overpunch is treated as unsigned")
        void shouldHandlePlainDigit() {
            // "000000019405" → no overpunch, just 19405 → 194.05
            var result = CobolDecimalParser.parseSignedDecimal("000000019405", 2);
            assertEquals(new BigDecimal("194.05"), result);
        }
    }
}
