package com.carddemo.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Account entity.
 * Validates field mapping from CVACT01Y.cpy ACCT-ID record layout.
 */
class AccountTest {

    @Test
    void testAccountCreation() {
        Account account = new Account();
        account.setAcctId(12345678901L);
        account.setAcctActiveStatus("Y");
        account.setAcctCurrBal(new BigDecimal("5000.50"));
        account.setAcctCreditLimit(new BigDecimal("10000.00"));
        account.setAcctCashCreditLimit(new BigDecimal("2000.00"));
        account.setAcctOpenDate("2023-01-15");
        account.setAcctExpirationDate("2028-01-15");
        account.setAcctReissueDate("2025-01-15");
        account.setAcctCurrCycCredit(new BigDecimal("1500.00"));
        account.setAcctCurrCycDebit(new BigDecimal("750.25"));
        account.setAcctAddrZip("10001");
        account.setAcctGroupId("GROUP001");

        assertEquals(12345678901L, account.getAcctId());
        assertEquals("Y", account.getAcctActiveStatus());
        assertEquals(new BigDecimal("5000.50"), account.getAcctCurrBal());
        assertEquals(new BigDecimal("10000.00"), account.getAcctCreditLimit());
        assertEquals(new BigDecimal("2000.00"), account.getAcctCashCreditLimit());
        assertEquals("2023-01-15", account.getAcctOpenDate());
        assertEquals("2028-01-15", account.getAcctExpirationDate());
        assertEquals("2025-01-15", account.getAcctReissueDate());
        assertEquals(new BigDecimal("1500.00"), account.getAcctCurrCycCredit());
        assertEquals(new BigDecimal("750.25"), account.getAcctCurrCycDebit());
        assertEquals("10001", account.getAcctAddrZip());
        assertEquals("GROUP001", account.getAcctGroupId());
    }

    @Test
    void testAccountDefaultValues() {
        Account account = new Account();
        assertNull(account.getAcctId());
        // Account has sensible defaults matching COBOL INITIALIZE behavior
        assertEquals("Y", account.getAcctActiveStatus());
        assertEquals(BigDecimal.ZERO, account.getAcctCurrBal());
    }

    @Test
    void testBigDecimalPrecision() {
        // Verify BigDecimal maintains financial precision (no floating-point errors)
        Account account = new Account();
        account.setAcctCurrBal(new BigDecimal("99999999.99"));
        assertEquals(new BigDecimal("99999999.99"), account.getAcctCurrBal());

        account.setAcctCurrBal(new BigDecimal("0.01"));
        assertEquals(new BigDecimal("0.01"), account.getAcctCurrBal());
    }
}
