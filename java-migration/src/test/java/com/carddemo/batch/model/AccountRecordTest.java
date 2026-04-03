package com.carddemo.batch.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountRecordTest {

    @Test
    void defaultConstructorInitializesDecimalsToZero() {
        AccountRecord rec = new AccountRecord();
        assertEquals(BigDecimal.ZERO, rec.getAcctCurrBal());
        assertEquals(BigDecimal.ZERO, rec.getAcctCreditLimit());
        assertEquals(BigDecimal.ZERO, rec.getAcctCashCreditLimit());
        assertEquals(BigDecimal.ZERO, rec.getAcctCurrCycCredit());
        assertEquals(BigDecimal.ZERO, rec.getAcctCurrCycDebit());
    }

    @Test
    void gettersAndSettersWork() {
        AccountRecord rec = new AccountRecord();
        rec.setAcctId(12345678901L);
        rec.setAcctActiveStatus("Y");
        rec.setAcctCurrBal(new BigDecimal("1500.50"));
        rec.setAcctCreditLimit(new BigDecimal("5000.00"));
        rec.setAcctCashCreditLimit(new BigDecimal("1000.00"));
        rec.setAcctOpenDate("2020-01-15");
        rec.setAcctExpirationDate("2025-01-15");
        rec.setAcctReissueDate("2023-06-01");
        rec.setAcctCurrCycCredit(new BigDecimal("200.00"));
        rec.setAcctCurrCycDebit(new BigDecimal("300.00"));
        rec.setAcctAddrZip("98101");
        rec.setAcctGroupId("GRP001");

        assertEquals(12345678901L, rec.getAcctId());
        assertEquals("Y", rec.getAcctActiveStatus());
        assertEquals(new BigDecimal("1500.50"), rec.getAcctCurrBal());
        assertEquals(new BigDecimal("5000.00"), rec.getAcctCreditLimit());
        assertEquals(new BigDecimal("1000.00"), rec.getAcctCashCreditLimit());
        assertEquals("2020-01-15", rec.getAcctOpenDate());
        assertEquals("2025-01-15", rec.getAcctExpirationDate());
        assertEquals("2023-06-01", rec.getAcctReissueDate());
        assertEquals(new BigDecimal("200.00"), rec.getAcctCurrCycCredit());
        assertEquals(new BigDecimal("300.00"), rec.getAcctCurrCycDebit());
        assertEquals("98101", rec.getAcctAddrZip());
        assertEquals("GRP001", rec.getAcctGroupId());
    }

    @Test
    void toStringContainsKeyFields() {
        AccountRecord rec = new AccountRecord();
        rec.setAcctId(11111111111L);
        rec.setAcctActiveStatus("Y");
        String str = rec.toString();
        assertTrue(str.contains("11111111111"));
        assertTrue(str.contains("Y"));
    }
}
