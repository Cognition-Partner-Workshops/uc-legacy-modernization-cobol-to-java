package com.carddemo.batch.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountRecordTest {

    @Test
    void parseFixedWidth_standardRecord() {
        String line = buildTestLine(
                "00000000001",            // ACCT-ID
                "Y",                       // ACTIVE-STATUS
                "000100000000",            // CURR-BAL: 1000000.00
                "000050000000",            // CREDIT-LIMIT: 500000.00
                "000025000000",            // CASH-CREDIT-LIMIT: 250000.00
                "2024-01-15",              // OPEN-DATE
                "2026-01-15",              // EXPIRATION-DATE
                "2025-06-15",              // REISSUE-DATE
                "000000050000",            // CURR-CYC-CREDIT: 500.00
                "000000030000",            // CURR-CYC-DEBIT: 300.00
                "10001     ",              // ADDR-ZIP
                "GROUP001  "               // GROUP-ID
        );

        AccountRecord rec = AccountRecord.parseFixedWidth(line);

        assertEquals(1L, rec.getAcctId());
        assertEquals('Y', rec.getActiveStatus());
        assertEquals(new BigDecimal("1000000.00"), rec.getCurrentBalance());
        assertEquals(new BigDecimal("500000.00"), rec.getCreditLimit());
        assertEquals(new BigDecimal("250000.00"), rec.getCashCreditLimit());
        assertEquals("2024-01-15", rec.getOpenDate());
        assertEquals("2026-01-15", rec.getExpirationDate());
        assertEquals("2025-06-15", rec.getReissueDate());
        assertEquals(new BigDecimal("500.00"), rec.getCurrentCycleCredit());
        assertEquals(new BigDecimal("300.00"), rec.getCurrentCycleDebit());
        assertEquals("10001     ", rec.getAddrZip());
        assertEquals("GROUP001  ", rec.getGroupId());
    }

    @Test
    void parseFixedWidth_zeroBalance() {
        String line = buildTestLine(
                "12345678901", "Y",
                "000000000000", "000000000000", "000000000000",
                "2024-01-01", "2026-01-01", "2025-01-01",
                "000000000000", "000000000000",
                "          ", "          "
        );

        AccountRecord rec = AccountRecord.parseFixedWidth(line);

        assertEquals(12345678901L, rec.getAcctId());
        assertEquals(BigDecimal.ZERO.setScale(2), rec.getCurrentBalance());
        assertEquals(BigDecimal.ZERO.setScale(2), rec.getCurrentCycleDebit());
    }

    @Test
    void parseFixedWidth_shortLine_isPadded() {
        String shortLine = "00000000001Y000000100000";
        AccountRecord rec = AccountRecord.parseFixedWidth(shortLine);
        assertEquals(1L, rec.getAcctId());
        assertEquals('Y', rec.getActiveStatus());
        assertEquals(new BigDecimal("1000.00"), rec.getCurrentBalance());
    }

    @Test
    void parseFixedWidth_negativeBalance() {
        String line = buildTestLine(
                "00000000001", "Y",
                "-00000100000", "000050000000", "000025000000",
                "2024-01-15", "2026-01-15", "2025-06-15",
                "000000050000", "-00000030000",
                "10001     ", "GROUP001  "
        );

        AccountRecord rec = AccountRecord.parseFixedWidth(line);
        assertEquals(new BigDecimal("-1000.00"), rec.getCurrentBalance());
        assertEquals(new BigDecimal("-300.00"), rec.getCurrentCycleDebit());
    }

    @Test
    void toFixedWidth_produces300Bytes() {
        AccountRecord rec = new AccountRecord();
        rec.setAcctId(1L);
        rec.setActiveStatus('Y');
        rec.setCurrentBalance(new BigDecimal("1000.00"));
        rec.setCreditLimit(new BigDecimal("5000.00"));
        rec.setCashCreditLimit(new BigDecimal("2500.00"));
        rec.setOpenDate("2024-01-15");
        rec.setExpirationDate("2026-01-15");
        rec.setReissueDate("2025-06-15");
        rec.setCurrentCycleCredit(new BigDecimal("500.00"));
        rec.setCurrentCycleDebit(new BigDecimal("300.00"));
        rec.setAddrZip("10001");
        rec.setGroupId("GROUP001");

        String fixedWidth = rec.toFixedWidth();
        assertEquals(AccountRecord.RECORD_LENGTH, fixedWidth.length());
    }

    @Test
    void roundTrip_parseAndFormat() {
        AccountRecord original = new AccountRecord();
        original.setAcctId(99999999999L);
        original.setActiveStatus('N');
        original.setCurrentBalance(new BigDecimal("12345678.90"));
        original.setCreditLimit(new BigDecimal("100000.00"));
        original.setCashCreditLimit(new BigDecimal("50000.00"));
        original.setOpenDate("2020-03-15");
        original.setExpirationDate("2025-03-15");
        original.setReissueDate("2023-06-30");
        original.setCurrentCycleCredit(new BigDecimal("1234.56"));
        original.setCurrentCycleDebit(new BigDecimal("789.01"));
        original.setAddrZip("90210     ");
        original.setGroupId("GRP999    ");

        String fixedWidth = original.toFixedWidth();
        AccountRecord parsed = AccountRecord.parseFixedWidth(fixedWidth);

        assertEquals(original.getAcctId(), parsed.getAcctId());
        assertEquals(original.getActiveStatus(), parsed.getActiveStatus());
        assertEquals(original.getCurrentBalance(), parsed.getCurrentBalance());
        assertEquals(original.getCreditLimit(), parsed.getCreditLimit());
        assertEquals(original.getOpenDate(), parsed.getOpenDate());
    }

    private String buildTestLine(String acctId, String status,
                                 String currBal, String creditLimit, String cashLimit,
                                 String openDate, String expDate, String reissueDate,
                                 String cycCredit, String cycDebit,
                                 String addrZip, String groupId) {
        StringBuilder sb = new StringBuilder(300);
        sb.append(acctId);       // 11
        sb.append(status);       // 1
        sb.append(currBal);      // 12
        sb.append(creditLimit);  // 12
        sb.append(cashLimit);    // 12
        sb.append(openDate);     // 10
        sb.append(expDate);      // 10
        sb.append(reissueDate);  // 10
        sb.append(cycCredit);    // 12
        sb.append(cycDebit);     // 12
        sb.append(addrZip);      // 10
        sb.append(groupId);      // 10
        // Pad filler to reach 300 bytes
        while (sb.length() < 300) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
