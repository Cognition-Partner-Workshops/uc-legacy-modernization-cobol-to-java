package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Java equivalent of the OUT-ACCT-REC file descriptor in CBACT01C.
 *
 * COBOL layout:
 *   05  OUT-ACCT-ID                PIC 9(11)
 *   05  OUT-ACCT-ACTIVE-STATUS     PIC X(01)
 *   05  OUT-ACCT-CURR-BAL          PIC S9(10)V99
 *   05  OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99
 *   05  OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *   05  OUT-ACCT-OPEN-DATE         PIC X(10)
 *   05  OUT-ACCT-EXPIRAION-DATE    PIC X(10)
 *   05  OUT-ACCT-REISSUE-DATE      PIC X(10)
 *   05  OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99
 *   05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99  USAGE IS COMP-3
 *   05  OUT-ACCT-GROUP-ID          PIC X(10)
 */
public record OutAccountRecord(
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

    /**
     * Format as a pipe-delimited line for the output file.
     * In COBOL this was a fixed-width binary record; in Java we use a
     * human-readable delimited format that preserves all field values.
     */
    public String toDelimitedLine() {
        return String.join("|",
                String.format("%011d", acctId),
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

    /**
     * Parse from a pipe-delimited line.
     */
    public static OutAccountRecord fromDelimitedLine(String line) {
        String[] parts = line.split("\\|", -1);
        return new OutAccountRecord(
                Long.parseLong(parts[0]),
                parts[1],
                new BigDecimal(parts[2]),
                new BigDecimal(parts[3]),
                new BigDecimal(parts[4]),
                parts[5],
                parts[6],
                parts[7],
                new BigDecimal(parts[8]),
                new BigDecimal(parts[9]),
                parts[10]
        );
    }
}
