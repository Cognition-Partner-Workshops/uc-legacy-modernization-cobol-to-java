package com.carddemo.batch.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountRecordTest {

    @Test
    @DisplayName("parseSignedDecimal handles positive value")
    void parsePositive() {
        assertEquals(new BigDecimal("1940.00"),
                AccountRecord.parseSignedDecimal("+00000194000"));
    }

    @Test
    @DisplayName("parseSignedDecimal handles negative value")
    void parseNegative() {
        assertEquals(new BigDecimal("-1025.00"),
                AccountRecord.parseSignedDecimal("-00000102500"));
    }

    @Test
    @DisplayName("parseSignedDecimal handles zero")
    void parseZero() {
        assertEquals(0, AccountRecord.parseSignedDecimal("+00000000000").signum());
    }

    @Test
    @DisplayName("formatSignedDecimal produces 12-char field")
    void formatLength() {
        String result = AccountRecord.formatSignedDecimal(new BigDecimal("1940.00"));
        assertEquals(12, result.length());
        assertTrue(result.startsWith("+"));
    }

    @Test
    @DisplayName("formatSignedDecimal round-trips with parseSignedDecimal")
    void formatParseRoundTrip() {
        BigDecimal original = new BigDecimal("6130.00");
        String formatted = AccountRecord.formatSignedDecimal(original);
        BigDecimal reparsed = AccountRecord.parseSignedDecimal(formatted);
        assertEquals(0, original.compareTo(reparsed));
    }

    @Test
    @DisplayName("parse and toFixedWidth round-trip preserves all fields")
    void fullRoundTrip() {
        AccountRecord original = new AccountRecord(
                42L, "Y",
                new BigDecimal("5000.00"), new BigDecimal("10000.00"),
                new BigDecimal("3000.00"),
                "2020-01-15", "2025-12-31", "2025-06-15",
                new BigDecimal("200.00"), new BigDecimal("150.50"),
                "12345", "GRP001");

        String line = original.toFixedWidth();
        assertEquals(300, line.length(), "Fixed-width line must be 300 chars");

        AccountRecord reparsed = AccountRecord.parse(line);
        assertEquals(original.acctId(), reparsed.acctId());
        assertEquals(original.activeStatus(), reparsed.activeStatus());
        assertEquals(0, original.currBal().compareTo(reparsed.currBal()));
        assertEquals(0, original.creditLimit().compareTo(reparsed.creditLimit()));
        assertEquals(0, original.cashCreditLimit().compareTo(reparsed.cashCreditLimit()));
        assertEquals(original.openDate(), reparsed.openDate());
        assertEquals(original.expirationDate(), reparsed.expirationDate());
        assertEquals(original.reissueDate(), reparsed.reissueDate());
        assertEquals(0, original.currCycCredit().compareTo(reparsed.currCycCredit()));
        assertEquals(0, original.currCycDebit().compareTo(reparsed.currCycDebit()));
    }
}
