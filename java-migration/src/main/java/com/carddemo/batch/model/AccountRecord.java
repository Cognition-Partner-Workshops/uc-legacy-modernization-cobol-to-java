package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Java equivalent of the CVACT01Y copybook (ACCOUNT-RECORD).
 *
 * COBOL layout (300 bytes total):
 *   05  ACCT-ID                     PIC 9(11)
 *   05  ACCT-ACTIVE-STATUS          PIC X(01)
 *   05  ACCT-CURR-BAL               PIC S9(10)V99
 *   05  ACCT-CREDIT-LIMIT           PIC S9(10)V99
 *   05  ACCT-CASH-CREDIT-LIMIT      PIC S9(10)V99
 *   05  ACCT-OPEN-DATE              PIC X(10)
 *   05  ACCT-EXPIRAION-DATE         PIC X(10)
 *   05  ACCT-REISSUE-DATE           PIC X(10)
 *   05  ACCT-CURR-CYC-CREDIT        PIC S9(10)V99
 *   05  ACCT-CURR-CYC-DEBIT         PIC S9(10)V99
 *   05  ACCT-ADDR-ZIP               PIC X(10)
 *   05  ACCT-GROUP-ID               PIC X(10)
 *   05  FILLER                      PIC X(178)
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

    /**
     * Format account ID as zero-padded 11-digit string (matching PIC 9(11)).
     */
    public String formattedAcctId() {
        return String.format("%011d", acctId);
    }

    @Override
    public String toString() {
        return "AccountRecord{" +
                "acctId=" + formattedAcctId() +
                ", activeStatus='" + activeStatus + '\'' +
                ", currBal=" + currBal +
                ", creditLimit=" + creditLimit +
                ", cashCreditLimit=" + cashCreditLimit +
                ", openDate='" + openDate + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                ", reissueDate='" + reissueDate + '\'' +
                ", currCycCredit=" + currCycCredit +
                ", currCycDebit=" + currCycDebit +
                ", addrZip='" + addrZip + '\'' +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}
