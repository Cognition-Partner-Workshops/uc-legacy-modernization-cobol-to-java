package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Java equivalent of COBOL copybook CVTRA02Y - Disclosure Group Record (RECLN 50).
 *
 * Original COBOL layout:
 *   05 DIS-GROUP-KEY
 *     10 DIS-ACCT-GROUP-ID  PIC X(10)
 *     10 DIS-TRAN-TYPE-CD   PIC X(02)
 *     10 DIS-TRAN-CAT-CD    PIC 9(04)
 *   05 DIS-INT-RATE          PIC S9(04)V99
 *   05 FILLER                PIC X(28)
 */
public class DisclosureGroupRecord {

    private String acctGroupId;
    private String tranTypeCd;
    private int tranCatCd;
    private BigDecimal interestRate;

    public DisclosureGroupRecord() {
        this.interestRate = BigDecimal.ZERO;
    }

    public DisclosureGroupRecord(String acctGroupId, String tranTypeCd, int tranCatCd,
                                  BigDecimal interestRate) {
        this.acctGroupId = acctGroupId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
        this.interestRate = interestRate;
    }

    public String getKey() {
        return String.format("%-10s%2s%04d", acctGroupId, tranTypeCd, tranCatCd);
    }

    public String getAcctGroupId() { return acctGroupId; }
    public void setAcctGroupId(String acctGroupId) { this.acctGroupId = acctGroupId; }

    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String tranTypeCd) { this.tranTypeCd = tranTypeCd; }

    public int getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(int tranCatCd) { this.tranCatCd = tranCatCd; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    @Override
    public String toString() {
        return "DisclosureGroupRecord{acctGroupId='" + acctGroupId + '\'' +
               ", tranTypeCd='" + tranTypeCd + '\'' +
               ", tranCatCd=" + tranCatCd +
               ", interestRate=" + interestRate +
               '}';
    }
}
