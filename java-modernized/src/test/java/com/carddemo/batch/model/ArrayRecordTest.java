package com.carddemo.batch.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ArrayRecordTest {

    @Test
    void initialisation_allZeros() {
        ArrayRecord arr = new ArrayRecord();
        assertEquals(5, arr.getEntries().size());
        for (BalanceDebitPair pair : arr.getEntries()) {
            assertEquals(BigDecimal.ZERO.setScale(2), pair.balance());
            assertEquals(BigDecimal.ZERO.setScale(2), pair.debit());
        }
    }

    @Test
    void setEntry_index0() {
        ArrayRecord arr = new ArrayRecord();
        arr.setEntry(0, new BigDecimal("1000.00"), new BigDecimal("1005.00"));
        assertEquals(new BigDecimal("1000.00"), arr.getEntries().get(0).balance());
        assertEquals(new BigDecimal("1005.00"), arr.getEntries().get(0).debit());
    }

    @Test
    void populationPattern_matchesCbact01c() {
        BigDecimal currentBalance = new BigDecimal("5000.00");

        ArrayRecord arr = new ArrayRecord();
        arr.setAcctId(12345678901L);
        arr.setEntry(0, currentBalance, new BigDecimal("1005.00"));
        arr.setEntry(1, currentBalance, new BigDecimal("1525.00"));
        arr.setEntry(2, new BigDecimal("-1025.00"), new BigDecimal("-2500.00"));

        assertEquals(12345678901L, arr.getAcctId());

        // Index 0: current balance + 1005.00 debit
        assertEquals(new BigDecimal("5000.00"), arr.getEntries().get(0).balance());
        assertEquals(new BigDecimal("1005.00"), arr.getEntries().get(0).debit());

        // Index 1: current balance + 1525.00 debit
        assertEquals(new BigDecimal("5000.00"), arr.getEntries().get(1).balance());
        assertEquals(new BigDecimal("1525.00"), arr.getEntries().get(1).debit());

        // Index 2: -1025.00 balance + -2500.00 debit
        assertEquals(new BigDecimal("-1025.00"), arr.getEntries().get(2).balance());
        assertEquals(new BigDecimal("-2500.00"), arr.getEntries().get(2).debit());

        // Indices 3-4: remain zero (COBOL INITIALIZE)
        assertEquals(BigDecimal.ZERO.setScale(2), arr.getEntries().get(3).balance());
        assertEquals(BigDecimal.ZERO.setScale(2), arr.getEntries().get(3).debit());
        assertEquals(BigDecimal.ZERO.setScale(2), arr.getEntries().get(4).balance());
        assertEquals(BigDecimal.ZERO.setScale(2), arr.getEntries().get(4).debit());
    }

    @Test
    void arraySize_alwaysFive() {
        assertEquals(5, ArrayRecord.ARRAY_SIZE);
    }
}
