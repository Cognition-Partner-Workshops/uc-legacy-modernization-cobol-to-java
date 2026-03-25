package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL VBRC-REC2 structure (second variable-length record).
 *
 * <pre>
 * 01 VBRC-REC2.
 *    05  VB2-ACCT-ID                PIC 9(11).
 *    05  VB2-ACCT-CURR-BAL          PIC S9(10)V99.
 *    05  VB2-ACCT-CREDIT-LIMIT      PIC S9(10)V99.
 *    05  VB2-ACCT-REISSUE-YYYY      PIC X(04).
 * </pre>
 *
 * Written with WS-RECD-LEN = 39 bytes.
 */
public record VbrcRecord2(
        long acctId,
        BigDecimal currBal,
        BigDecimal creditLimit,
        String reissueYyyy
) {

    /** Record length as set by MOVE 39 TO WS-RECD-LEN. */
    public static final int RECORD_LENGTH = 39;

    public static VbrcRecord2 fromAccountRecord(AccountRecord input) {
        String yyyy = input.reissueDate().length() >= 4
                ? input.reissueDate().substring(0, 4)
                : input.reissueDate();
        return new VbrcRecord2(input.acctId(), input.currBal(), input.creditLimit(), yyyy);
    }
}
