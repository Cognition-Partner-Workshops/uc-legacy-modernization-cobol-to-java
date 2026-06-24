package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Mirrors ARR-ARRAY-REC from CBACT01C.cbl (FD ARRY-FILE).
 *
 * Layout:
 *   ARR-ACCT-ID        PIC 9(11)
 *   ARR-ACCT-BAL       OCCURS 5 TIMES
 *     ARR-ACCT-CURR-BAL       PIC S9(10)V99
 *     ARR-ACCT-CURR-CYC-DEBIT PIC S9(10)V99 USAGE IS COMP-3
 *   ARR-FILLER         PIC X(04)
 *
 * Each array slot holds a balance/debit pair.
 */
public class ArrayRecord {

    public static final int ARRAY_SIZE = 5;

    private long acctId;
    private final List<BalanceDebitPair> entries;

    public ArrayRecord() {
        this.entries = new ArrayList<>(ARRAY_SIZE);
        for (int i = 0; i < ARRAY_SIZE; i++) {
            entries.add(BalanceDebitPair.zero());
        }
    }

    public long getAcctId() { return acctId; }
    public void setAcctId(long acctId) { this.acctId = acctId; }

    public List<BalanceDebitPair> getEntries() { return entries; }

    public void setEntry(int index, BigDecimal balance, BigDecimal debit) {
        entries.set(index, new BalanceDebitPair(
                balance.setScale(2),
                debit.setScale(2)));
    }

    public String toDelimited(String delimiter) {
        StringBuilder sb = new StringBuilder();
        sb.append(acctId);
        for (BalanceDebitPair pair : entries) {
            sb.append(delimiter).append(pair.balance().toPlainString());
            sb.append(delimiter).append(pair.debit().toPlainString());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ArrayRecord{acctId=" + acctId + ", entries=" + entries + '}';
    }
}
