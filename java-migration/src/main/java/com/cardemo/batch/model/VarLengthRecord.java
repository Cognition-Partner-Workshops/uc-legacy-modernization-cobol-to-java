package com.cardemo.batch.model;

import java.math.BigDecimal;

/**
 * Sealed interface modelling the two variable-length record types written
 * to VBRC-FILE by CBACT01C.
 */
public sealed interface VarLengthRecord {

    /**
     * Short record (12 bytes): account ID + active status.
     *
     * <pre>
     * 01 VBRC-REC1.
     *    05  VB1-ACCT-ID             PIC 9(11).
     *    05  VB1-ACCT-ACTIVE-STATUS  PIC X(01).
     * </pre>
     */
    record StatusRecord(long acctId, char activeStatus) implements VarLengthRecord {
        public static final int LENGTH = 12;
    }

    /**
     * Long record (39 bytes): account ID + balances + reissue year.
     *
     * <pre>
     * 01 VBRC-REC2.
     *    05  VB2-ACCT-ID             PIC 9(11).
     *    05  VB2-ACCT-CURR-BAL       PIC S9(10)V99.
     *    05  VB2-ACCT-CREDIT-LIMIT   PIC S9(10)V99.
     *    05  VB2-ACCT-REISSUE-YYYY   PIC X(04).
     * </pre>
     */
    record BalanceRecord(long acctId, BigDecimal currBal,
                         BigDecimal creditLimit, String reissueYear) implements VarLengthRecord {
        public static final int LENGTH = 39;
    }
}
