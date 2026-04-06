package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps the COBOL OUT-ACCT-REC file-descriptor structure written to OUTFILE.
 * <pre>
 *   05  OUT-ACCT-ID                PIC 9(11)
 *   05  OUT-ACCT-ACTIVE-STATUS     PIC X(01)
 *   05  OUT-ACCT-CURR-BAL          PIC S9(10)V99
 *   05  OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99
 *   05  OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *   05  OUT-ACCT-OPEN-DATE         PIC X(10)
 *   05  OUT-ACCT-EXPIRAION-DATE    PIC X(10)
 *   05  OUT-ACCT-REISSUE-DATE      PIC X(10)
 *   05  OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99
 *   05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99 USAGE IS COMP-3
 *   05  OUT-ACCT-GROUP-ID          PIC X(10)
 * </pre>
 * Note: CURR-CYC-DEBIT is COMP-3 (packed decimal) in the COBOL version.
 * In the Java version we store it as BigDecimal and serialise to a text
 * representation for the output file.
 */
public record OutputAccountRecord(
        String acctId,                   // PIC 9(11)
        String acctActiveStatus,         // PIC X(01)
        BigDecimal acctCurrBal,          // PIC S9(10)V99
        BigDecimal acctCreditLimit,      // PIC S9(10)V99
        BigDecimal acctCashCreditLimit,  // PIC S9(10)V99
        String acctOpenDate,             // PIC X(10)
        String acctExpirationDate,       // PIC X(10)
        String acctReissueDate,          // PIC X(10)  — YYYYMMDD + 2 spaces
        BigDecimal acctCurrCycCredit,    // PIC S9(10)V99
        BigDecimal acctCurrCycDebit,     // PIC S9(10)V99 COMP-3
        String acctGroupId              // PIC X(10)
) {
    /**
     * Serialises to a pipe-delimited text line for the output file.
     * This replaces the COBOL binary WRITE with a human-readable equivalent
     * that preserves all field values for verification.
     */
    public String toDelimitedLine() {
        return String.join("|",
                acctId,
                acctActiveStatus,
                acctCurrBal.toPlainString(),
                acctCreditLimit.toPlainString(),
                acctCashCreditLimit.toPlainString(),
                acctOpenDate,
                acctExpirationDate,
                acctReissueDate,
                acctCurrCycCredit.toPlainString(),
                acctCurrCycDebit.toPlainString(),
                acctGroupId
        );
    }
}
