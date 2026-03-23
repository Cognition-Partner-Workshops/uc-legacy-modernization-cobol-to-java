package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Java equivalent of COBOL copybook CVTRA01Y - Transaction Category Balance (RECLN 50).
 *
 * Original COBOL layout:
 *   05 TRAN-CAT-KEY
 *     10 TRANCAT-ACCT-ID   PIC 9(11)
 *     10 TRANCAT-TYPE-CD   PIC X(02)
 *     10 TRANCAT-CD        PIC 9(04)
 *   05 TRAN-CAT-BAL        PIC S9(09)V99
 *   05 FILLER              PIC X(22)
 */
public class TranCatBalRecord {

    private long acctId;
    private String typeCd;
    private int catCd;
    private BigDecimal balance;

    public TranCatBalRecord() {
        this.balance = BigDecimal.ZERO;
    }

    public TranCatBalRecord(long acctId, String typeCd, int catCd, BigDecimal balance) {
        this.acctId = acctId;
        this.typeCd = typeCd;
        this.catCd = catCd;
        this.balance = balance;
    }

    public String getKey() {
        return String.format("%011d%2s%04d", acctId, typeCd, catCd);
    }

    public long getAcctId() { return acctId; }
    public void setAcctId(long acctId) { this.acctId = acctId; }

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }

    public int getCatCd() { return catCd; }
    public void setCatCd(int catCd) { this.catCd = catCd; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    @Override
    public String toString() {
        return "TranCatBalRecord{acctId=" + acctId +
               ", typeCd='" + typeCd + '\'' +
               ", catCd=" + catCd +
               ", balance=" + balance +
               '}';
    }
}
