package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL copybook CVACT01Y – the 300-byte ACCOUNT-RECORD layout.
 * <p>
 * Field positions (1-based, character offsets in the fixed-width ASCII file):
 * <pre>
 *   ACCT-ID                PIC 9(11)       positions   1–11
 *   ACCT-ACTIVE-STATUS     PIC X(01)       position   12
 *   ACCT-CURR-BAL          PIC S9(10)V99   positions  13–24
 *   ACCT-CREDIT-LIMIT      PIC S9(10)V99   positions  25–36
 *   ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99   positions  37–48
 *   ACCT-OPEN-DATE         PIC X(10)       positions  49–58
 *   ACCT-EXPIRAION-DATE    PIC X(10)       positions  59–68
 *   ACCT-REISSUE-DATE      PIC X(10)       positions  69–78
 *   ACCT-CURR-CYC-CREDIT   PIC S9(10)V99   positions  79–90
 *   ACCT-CURR-CYC-DEBIT    PIC S9(10)V99   positions  91–102
 *   ACCT-ADDR-ZIP          PIC X(10)       positions 103–112
 *   ACCT-GROUP-ID          PIC X(10)       positions 113–122
 *   FILLER                 PIC X(178)      positions 123–300
 * </pre>
 */
public record AccountRecord(
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
        String addrZip,
        String groupId
) {

    /** Total record length in the fixed-width file. */
    public static final int RECORD_LENGTH = 300;
}
