package com.carddemo.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EBCDIC migration utility.
 * Tests packed decimal extraction and alphanumeric field parsing.
 */
class EbcdicMigrationUtilityTest {

    @Test
    void testExtractPackedDecimalPositive() {
        EbcdicMigrationUtility util = new EbcdicMigrationUtility("CP037");

        // Packed decimal 01 23 45 0C = digits 0123450, sign C (positive) = 1234.50 with scale 2
        byte[] record = new byte[10];
        record[0] = 0x01;
        record[1] = 0x23;
        record[2] = 0x45;
        record[3] = 0x0C; // positive sign

        BigDecimal result = util.extractPackedDecimal(record, 0, 4, 2);
        assertEquals(new BigDecimal("1234.50"), result);
    }

    @Test
    void testExtractPackedDecimalNegative() {
        EbcdicMigrationUtility util = new EbcdicMigrationUtility("CP037");

        // -500 in packed decimal with scale 2: 00 50 00 0D (4 bytes)
        byte[] record = new byte[10];
        record[0] = 0x00;
        record[1] = 0x50;
        record[2] = 0x00;
        record[3] = 0x0D; // negative sign

        BigDecimal result = util.extractPackedDecimal(record, 0, 4, 2);
        assertEquals(new BigDecimal("-500.00"), result);
    }

    @Test
    void testExtractPackedDecimalZero() {
        EbcdicMigrationUtility util = new EbcdicMigrationUtility("CP037");

        // 0 in packed decimal: 00 0C (2 bytes)
        byte[] record = new byte[10];
        record[0] = 0x00;
        record[1] = 0x0C;

        BigDecimal result = util.extractPackedDecimal(record, 0, 2, 2);
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void testExtractAlphanumericWithTrailingSpaces() {
        EbcdicMigrationUtility util = new EbcdicMigrationUtility("CP037");

        // "HELLO   " in EBCDIC CP037
        // H=0xC8, E=0xC5, L=0xD3, L=0xD3, O=0xD6, space=0x40
        byte[] record = new byte[8];
        record[0] = (byte) 0xC8; // H
        record[1] = (byte) 0xC5; // E
        record[2] = (byte) 0xD3; // L
        record[3] = (byte) 0xD3; // L
        record[4] = (byte) 0xD6; // O
        record[5] = (byte) 0x40; // space
        record[6] = (byte) 0x40; // space
        record[7] = (byte) 0x40; // space

        String result = util.extractAlphanumeric(record, 0, 8);
        assertEquals("HELLO", result);
    }
}
