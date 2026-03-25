package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL FD ARRY-FILE record (ARR-ARRAY-REC).
 *
 * <pre>
 *  05  ARR-ACCT-ID                PIC 9(11)
 *  05  ARR-ACCT-BAL OCCURS 5 TIMES
 *    10  ARR-ACCT-CURR-BAL        PIC S9(10)V99
 *    10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 COMP-3
 *  05  ARR-FILLER                 PIC X(04)
 * </pre>
 *
 * Each of the five balance slots holds a current-balance / cycle-debit pair.
 */
public final class ArrayRecord {

    public static final int SLOT_COUNT = 5;

    private long acctId;
    private final BigDecimal[] acctCurrBal = new BigDecimal[SLOT_COUNT];
    private final BigDecimal[] acctCurrCycDebit = new BigDecimal[SLOT_COUNT];

    public ArrayRecord() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            acctCurrBal[i] = BigDecimal.ZERO;
            acctCurrCycDebit[i] = BigDecimal.ZERO;
        }
    }

    public long getAcctId() { return acctId; }
    public void setAcctId(long acctId) { this.acctId = acctId; }

    public BigDecimal getAcctCurrBal(int index) { return acctCurrBal[index]; }
    public void setAcctCurrBal(int index, BigDecimal value) { acctCurrBal[index] = value; }

    public BigDecimal getAcctCurrCycDebit(int index) { return acctCurrCycDebit[index]; }
    public void setAcctCurrCycDebit(int index, BigDecimal value) { acctCurrCycDebit[index] = value; }

    public BigDecimal[] getAllCurrBal() { return acctCurrBal.clone(); }
    public BigDecimal[] getAllCurrCycDebit() { return acctCurrCycDebit.clone(); }
}
