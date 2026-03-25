package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Output account record — corresponds to the OUT-FILE FD in CBACT01C.
 *
 * COBOL equivalent:
 * <pre>
 *   FD OUT-FILE.
 *   01 OUT-ACCT-REC.
 *      05  OUT-ACCT-ID                PIC 9(11).
 *      05  OUT-ACCT-ACTIVE-STATUS     PIC X(01).
 *      05  OUT-ACCT-CURR-BAL          PIC S9(10)V99.
 *      05  OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99.
 *      05  OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99.
 *      05  OUT-ACCT-OPEN-DATE         PIC X(10).
 *      05  OUT-ACCT-EXPIRAION-DATE    PIC X(10).
 *      05  OUT-ACCT-REISSUE-DATE      PIC X(10).
 *      05  OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99.
 *      05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99 USAGE IS COMP-3.
 *      05  OUT-ACCT-GROUP-ID          PIC X(10).
 * </pre>
 *
 * Note: In COBOL, OUT-ACCT-CURR-CYC-DEBIT is COMP-3 (packed decimal).
 * In Java, we use BigDecimal for all financial fields.
 */
public record OutAccountRecord(
        long acctId,
        String activeStatus,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currentCycleCredit,
        BigDecimal currentCycleDebit,
        String groupId
) {
}
