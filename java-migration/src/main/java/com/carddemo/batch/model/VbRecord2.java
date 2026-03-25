package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL VBRC-REC2 – variable-length record type 2 (39 bytes).
 * Contains account ID, current balance, credit limit, and reissue year.
 */
public record VbRecord2(
        long acctId,
        BigDecimal currBal,
        BigDecimal creditLimit,
        String reissueYear
) {
}
