package com.carddemo.batch.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRecordTest {

    @Test
    void defaultConstructorInitializesAmountToZero() {
        TransactionRecord rec = new TransactionRecord();
        assertEquals(BigDecimal.ZERO, rec.getTranAmt());
    }

    @Test
    void gettersAndSettersWork() {
        TransactionRecord rec = new TransactionRecord();
        rec.setTranId("TRN0000001");
        rec.setTranTypeCd("01");
        rec.setTranCatCd(5);
        rec.setTranSource("System");
        rec.setTranDesc("Interest charge");
        rec.setTranAmt(new BigDecimal("125.50"));
        rec.setTranMerchantId(0);
        rec.setTranMerchantName("");
        rec.setTranMerchantCity("");
        rec.setTranMerchantZip("");
        rec.setTranCardNum("4111111111111111");
        rec.setTranOrigTs("2025-01-15-10.30.00.000000");
        rec.setTranProcTs("2025-01-15-10.30.01.000000");

        assertEquals("TRN0000001", rec.getTranId());
        assertEquals("01", rec.getTranTypeCd());
        assertEquals(5, rec.getTranCatCd());
        assertEquals("System", rec.getTranSource());
        assertEquals("Interest charge", rec.getTranDesc());
        assertEquals(new BigDecimal("125.50"), rec.getTranAmt());
        assertEquals(0, rec.getTranMerchantId());
        assertEquals("4111111111111111", rec.getTranCardNum());
    }

    @Test
    void toStringContainsKeyFields() {
        TransactionRecord rec = new TransactionRecord();
        rec.setTranId("TEST123");
        rec.setTranTypeCd("02");
        String str = rec.toString();
        assertTrue(str.contains("TEST123"));
        assertTrue(str.contains("02"));
    }
}
