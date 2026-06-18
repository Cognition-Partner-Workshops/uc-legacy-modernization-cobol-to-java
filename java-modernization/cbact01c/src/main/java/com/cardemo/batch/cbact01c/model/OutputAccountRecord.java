package com.cardemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * Mirrors OUT-ACCT-REC — the flat-file extract written to OUTFILE.
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
 *    05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99  USAGE IS COMP-3.
 *    05  OUT-ACCT-GROUP-ID          PIC X(10).
 * </pre>
 *
 * Note: In the original COBOL, OUT-ACCT-CURR-CYC-DEBIT is COMP-3 (packed decimal,
 * 7 bytes on disk). In this Java rewrite it is stored as BigDecimal and serialised
 * to packed-decimal bytes only when writing the legacy-compatible binary format.
 */
public record OutputAccountRecord(
        long acctId,
        char activeStatus,
        BigDecimal currBal,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currCycCredit,
        BigDecimal currCycDebit,
        String groupId
) {}
