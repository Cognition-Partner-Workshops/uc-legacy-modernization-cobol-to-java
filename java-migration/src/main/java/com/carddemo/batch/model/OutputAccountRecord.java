package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL FD OUT-FILE record (OUT-ACCT-REC).
 * <p>
 * Contains selected and transformed fields from the input {@link AccountRecord},
 * written to the sequential output file.
 */
public record OutputAccountRecord(
        long acctId,
        String activeStatus,
        BigDecimal currBal,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,        // reformatted via COBDATFT (YYYYMMDD)
        BigDecimal currCycCredit,
        BigDecimal currCycDebit,    // replaced with 2525.00 when input is zero
        String groupId
) {
}
