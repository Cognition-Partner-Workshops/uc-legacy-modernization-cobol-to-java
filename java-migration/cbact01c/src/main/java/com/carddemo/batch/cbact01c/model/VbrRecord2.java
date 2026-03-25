package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * Java equivalent of COBOL VBRC-REC2 (variable-length record type 2, 39 bytes).
 * <p>
 * Contains account ID, balance, credit limit, and reissue year.
 */
public record VbrRecord2(
        long acctId,                // PIC 9(11)
        BigDecimal currBal,         // PIC S9(10)V99
        BigDecimal creditLimit,     // PIC S9(10)V99
        String reissueYear          // PIC X(04) -- first 4 chars of reissue date
) {
    public static final int LENGTH = 39;
}
