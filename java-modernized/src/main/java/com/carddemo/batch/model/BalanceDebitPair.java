package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Represents one entry in the ARR-ACCT-BAL OCCURS 5 array.
 * Each entry holds a current balance and a cycle debit amount.
 */
public record BalanceDebitPair(BigDecimal balance, BigDecimal debit) {

    public static BalanceDebitPair zero() {
        return new BalanceDebitPair(
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2));
    }
}
