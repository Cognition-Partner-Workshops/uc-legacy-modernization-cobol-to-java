package com.carddemo.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Transaction entity.
 * Validates field mapping from CVTRA05Y.cpy TRAN-RECORD layout.
 */
class TransactionTest {

    @Test
    void testTransactionCreation() {
        Transaction tran = new Transaction();
        tran.setTranId("0000000000000001");
        tran.setTranTypeCd("PR");
        tran.setTranCatCd(1);
        tran.setTranSource("ONLINE");
        tran.setTranDesc("Grocery purchase");
        tran.setTranAmt(new BigDecimal("125.50"));
        tran.setTranMerchantId(999L);
        tran.setTranMerchantName("SUPERMART");
        tran.setTranMerchantCity("NEW YORK");
        tran.setTranMerchantZip("10001");
        tran.setTranCardNum("4111111111111111");
        tran.setTranOrigTs("2025-01-15 10:30:00.000000");

        assertEquals("0000000000000001", tran.getTranId());
        assertEquals("PR", tran.getTranTypeCd());
        assertEquals(1, tran.getTranCatCd());
        assertEquals(new BigDecimal("125.50"), tran.getTranAmt());
        assertEquals("4111111111111111", tran.getTranCardNum());
    }

    @Test
    void testTransactionAmountPrecision() {
        Transaction tran = new Transaction();
        tran.setTranAmt(new BigDecimal("999999.99"));
        assertEquals(new BigDecimal("999999.99"), tran.getTranAmt());
    }
}
