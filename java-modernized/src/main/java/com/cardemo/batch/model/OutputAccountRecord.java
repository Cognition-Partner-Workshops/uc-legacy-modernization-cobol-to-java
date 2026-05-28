package com.cardemo.batch.model;

import java.math.BigDecimal;

/**
 * Corresponds to COBOL FD OUT-FILE record (OUT-ACCT-REC).
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
 *    05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99 COMP-3.
 *    05  OUT-ACCT-GROUP-ID          PIC X(10).
 * </pre>
 */
public record OutputAccountRecord(
        long acctId,
        String activeStatus,
        BigDecimal currBal,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currCycCredit,
        BigDecimal currCycDebit,
        String groupId
) {

    public String toCsv() {
        return String.join(",",
                String.valueOf(acctId),
                activeStatus,
                currBal.toPlainString(),
                creditLimit.toPlainString(),
                cashCreditLimit.toPlainString(),
                openDate,
                expirationDate,
                reissueDate,
                currCycCredit.toPlainString(),
                currCycDebit.toPlainString(),
                groupId
        );
    }

    public static String csvHeader() {
        return "ACCT_ID,ACTIVE_STATUS,CURR_BAL,CREDIT_LIMIT,CASH_CREDIT_LIMIT,"
                + "OPEN_DATE,EXPIRATION_DATE,REISSUE_DATE,CURR_CYC_CREDIT,"
                + "CURR_CYC_DEBIT,GROUP_ID";
    }
}
