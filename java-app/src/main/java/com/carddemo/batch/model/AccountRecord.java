package com.carddemo.batch.model;

import com.carddemo.batch.io.ZonedDecimalUtil;

import java.math.BigDecimal;

/**
 * Maps CVACT01Y.cpy — the 300-byte account master record.
 *
 * Field layout (all DISPLAY unless noted):
 *   ACCT-ID                  PIC 9(11)         offset  0  len 11
 *   ACCT-ACTIVE-STATUS       PIC X(01)         offset 11  len  1
 *   ACCT-CURR-BAL            PIC S9(10)V99     offset 12  len 12
 *   ACCT-CREDIT-LIMIT        PIC S9(10)V99     offset 24  len 12
 *   ACCT-CASH-CREDIT-LIMIT   PIC S9(10)V99     offset 36  len 12
 *   ACCT-OPEN-DATE           PIC X(10)         offset 48  len 10
 *   ACCT-EXPIRAION-DATE      PIC X(10)         offset 58  len 10
 *   ACCT-REISSUE-DATE        PIC X(10)         offset 68  len 10
 *   ACCT-CURR-CYC-CREDIT     PIC S9(10)V99     offset 78  len 12
 *   ACCT-CURR-CYC-DEBIT      PIC S9(10)V99     offset 90  len 12
 *   ACCT-ADDR-ZIP            PIC X(10)         offset 102 len 10
 *   ACCT-GROUP-ID            PIC X(10)         offset 112 len 10
 *   FILLER                   PIC X(178)        offset 122 len 178
 *                                              total      300
 */
public record AccountRecord(
        String acctId,
        String acctActiveStatus,
        BigDecimal acctCurrBal,
        BigDecimal acctCreditLimit,
        BigDecimal acctCashCreditLimit,
        String acctOpenDate,
        String acctExpiraionDate,
        String acctReissueDate,
        BigDecimal acctCurrCycCredit,
        BigDecimal acctCurrCycDebit,
        String acctAddrZip,
        String acctGroupId
) {

    public static final int RECORD_LENGTH = 300;

    public static AccountRecord parse(String line) {
        if (line.length() < RECORD_LENGTH) {
            line = String.format("%-" + RECORD_LENGTH + "s", line);
        }

        String acctId             = line.substring(0, 11);
        String activeStatus       = line.substring(11, 12);
        BigDecimal currBal        = ZonedDecimalUtil.parse(line.substring(12, 24), 2);
        BigDecimal creditLimit    = ZonedDecimalUtil.parse(line.substring(24, 36), 2);
        BigDecimal cashCreditLim  = ZonedDecimalUtil.parse(line.substring(36, 48), 2);
        String openDate           = line.substring(48, 58);
        String expiraionDate      = line.substring(58, 68);
        String reissueDate        = line.substring(68, 78);
        BigDecimal currCycCredit  = ZonedDecimalUtil.parse(line.substring(78, 90), 2);
        BigDecimal currCycDebit   = ZonedDecimalUtil.parse(line.substring(90, 102), 2);
        String addrZip            = line.substring(102, 112);
        String groupId            = line.substring(112, 122);

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLim,
                openDate, expiraionDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId);
    }

    public String displayLines() {
        return "ACCT-ID                 :" + acctId + "\n" +
               "ACCT-ACTIVE-STATUS      :" + acctActiveStatus + "\n" +
               "ACCT-CURR-BAL           :" + ZonedDecimalUtil.format(acctCurrBal, 12, 2) + "\n" +
               "ACCT-CREDIT-LIMIT       :" + ZonedDecimalUtil.format(acctCreditLimit, 12, 2) + "\n" +
               "ACCT-CASH-CREDIT-LIMIT  :" + ZonedDecimalUtil.format(acctCashCreditLimit, 12, 2) + "\n" +
               "ACCT-OPEN-DATE          :" + acctOpenDate + "\n" +
               "ACCT-EXPIRAION-DATE     :" + acctExpiraionDate + "\n" +
               "ACCT-REISSUE-DATE       :" + acctReissueDate + "\n" +
               "ACCT-CURR-CYC-CREDIT    :" + ZonedDecimalUtil.format(acctCurrCycCredit, 12, 2) + "\n" +
               "ACCT-CURR-CYC-DEBIT     :" + ZonedDecimalUtil.format(acctCurrCycDebit, 12, 2) + "\n" +
               "ACCT-GROUP-ID           :" + acctGroupId + "\n" +
               "-------------------------------------------------";
    }

    public String toRawLine() {
        return acctId +
               acctActiveStatus +
               ZonedDecimalUtil.format(acctCurrBal, 12, 2) +
               ZonedDecimalUtil.format(acctCreditLimit, 12, 2) +
               ZonedDecimalUtil.format(acctCashCreditLimit, 12, 2) +
               acctOpenDate +
               acctExpiraionDate +
               acctReissueDate +
               ZonedDecimalUtil.format(acctCurrCycCredit, 12, 2) +
               ZonedDecimalUtil.format(acctCurrCycDebit, 12, 2) +
               acctAddrZip +
               acctGroupId;
    }
}
