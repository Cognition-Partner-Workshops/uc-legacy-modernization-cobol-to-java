package com.cardemo.batch.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountRecordTest {

    private static String buildLine(
            String acctId, String status,
            String currBal, String creditLimit, String cashCredit,
            String openDate, String expDate, String reissueDate,
            String cycCredit, String cycDebit,
            String zip, String groupId) {
        StringBuilder sb = new StringBuilder(300);
        sb.append(acctId);       // 11
        sb.append(status);       // 1
        sb.append(currBal);      // 12
        sb.append(creditLimit);  // 12
        sb.append(cashCredit);   // 12
        sb.append(openDate);     // 10
        sb.append(expDate);      // 10
        sb.append(reissueDate);  // 10
        sb.append(cycCredit);    // 12
        sb.append(cycDebit);     // 12
        sb.append(zip);          // 10
        sb.append(groupId);      // 10
        while (sb.length() < 300) {
            sb.append(' ');
        }
        return sb.toString();
    }

    @Test
    void parse_validLine_extractsAllFields() {
        String line = buildLine(
                "00000000042", "Y",
                "+00000100000", "+00001000000", "+00000500000",
                "2020-01-15", "2025-12-31", "2023-06-01",
                "+00000005000", "+00000003000",
                "10001     ", "GRP001    "
        );

        AccountRecord rec = AccountRecord.parse(line);

        assertEquals(42L, rec.acctId());
        assertEquals("Y", rec.activeStatus());
        assertEquals(new BigDecimal("1000.00"), rec.currentBalance());
        assertEquals(new BigDecimal("10000.00"), rec.creditLimit());
        assertEquals(new BigDecimal("5000.00"), rec.cashCreditLimit());
        assertEquals("2020-01-15", rec.openDate());
        assertEquals("2025-12-31", rec.expirationDate());
        assertEquals("2023-06-01", rec.reissueDate());
        assertEquals(new BigDecimal("50.00"), rec.currentCycleCredit());
        assertEquals(new BigDecimal("30.00"), rec.currentCycleDebit());
        assertEquals("10001     ", rec.addressZip());
        assertEquals("GRP001    ", rec.groupId());
    }

    @Test
    void parse_negativeBalance_handledCorrectly() {
        String line = buildLine(
                "00000000003", "N",
                "-00000050000", "+00001500000", "+00000750000",
                "2018-07-20", "2023-07-20", "2021-01-10",
                "+00000002500", "+00000001200",
                "30003     ", "GRP003    "
        );

        AccountRecord rec = AccountRecord.parse(line);

        assertEquals(new BigDecimal("-500.00"), rec.currentBalance());
    }

    @Test
    void parse_zeroBalance_handledCorrectly() {
        String line = buildLine(
                "00000000099", "Y",
                "+00000000000", "+00001000000", "+00000500000",
                "2020-01-15", "2025-12-31", "2023-06-01",
                "+00000000000", "+00000000000",
                "10001     ", "GRP001    "
        );

        AccountRecord rec = AccountRecord.parse(line);

        assertEquals(BigDecimal.ZERO.setScale(2), rec.currentBalance().setScale(2));
        assertEquals(BigDecimal.ZERO.setScale(2), rec.currentCycleDebit().setScale(2));
    }

    @Test
    void parse_shortLine_paddedToRecordLength() {
        String shortLine = "00000000001Y";
        assertDoesNotThrow(() -> AccountRecord.parse(shortLine));
    }

    @Test
    void formatDisplay_containsExpectedFields() {
        AccountRecord rec = new AccountRecord(
                1L, "Y",
                new BigDecimal("1000.00"), new BigDecimal("10000.00"), new BigDecimal("5000.00"),
                "2020-01-15", "2025-12-31", "2023-06-01",
                new BigDecimal("50.00"), new BigDecimal("30.00"),
                "10001     ", "GRP001    "
        );

        String display = rec.formatDisplay();
        assertTrue(display.contains("ACCT-ID"));
        assertTrue(display.contains("00000000001"));
        assertTrue(display.contains("ACCT-ACTIVE-STATUS"));
        assertTrue(display.contains("-------------------------------------------------"));
    }
}
