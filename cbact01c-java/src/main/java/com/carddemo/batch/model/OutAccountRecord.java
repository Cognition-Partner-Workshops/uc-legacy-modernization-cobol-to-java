package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL FD OUT-FILE / OUT-ACCT-REC (LRECL=107 from JCL).
 *
 * <pre>
 * 01 OUT-ACCT-REC.
 *    05  OUT-ACCT-ID                PIC 9(11).
 *    05  OUT-ACCT-ACTIVE-STATUS     PIC X(01).
 *    05  OUT-ACCT-CURR-BAL          PIC S9(10)V99.
 *    05  OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99.
 *    05  OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99.
 *    05  OUT-ACCT-OPEN-DATE         PIC X(10).
 *    05  OUT-ACCT-EXPIRAION-DATE    PIC X(10).
 *    05  OUT-ACCT-REISSUE-DATE      PIC X(10).
 *    05  OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99.
 *    05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99 USAGE IS COMP-3.
 *    05  OUT-ACCT-GROUP-ID          PIC X(10).
 * </pre>
 *
 * Note: OUT-ACCT-CURR-CYC-DEBIT uses COMP-3 (packed decimal) in COBOL,
 * which is 7 bytes for PIC S9(10)V99. In our Java model we store the
 * logical value and handle serialisation separately.
 */
public record OutAccountRecord(
        long acctId,                    // PIC 9(11)          11 bytes
        String activeStatus,            // PIC X(01)           1 byte
        BigDecimal currBal,             // PIC S9(10)V99      12 bytes (zoned)
        BigDecimal creditLimit,         // PIC S9(10)V99      12 bytes (zoned)
        BigDecimal cashCreditLimit,     // PIC S9(10)V99      12 bytes (zoned)
        String openDate,                // PIC X(10)          10 bytes
        String expirationDate,          // PIC X(10)          10 bytes
        String reissueDate,             // PIC X(10)          10 bytes
        BigDecimal currCycCredit,       // PIC S9(10)V99      12 bytes (zoned)
        BigDecimal currCycDebit,        // PIC S9(10)V99 COMP-3  7 bytes (packed)
        String groupId                  // PIC X(10)          10 bytes
) {
    // Total: 11+1+12+12+12+10+10+10+12+7+10 = 107 bytes (matches JCL LRECL)
}
