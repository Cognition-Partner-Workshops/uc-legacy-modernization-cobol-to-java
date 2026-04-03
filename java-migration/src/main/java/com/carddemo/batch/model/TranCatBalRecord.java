package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Java equivalent of COBOL copybook CVTRA01Y - Transaction category balance (RECLN 50).
 * Maps to: TRAN-CAT-BAL-RECORD in COBOL.
 */
public class TranCatBalRecord {

    private long trancatAcctId;      // PIC 9(11)
    private String trancatTypeCd;    // PIC X(02)
    private int trancatCd;           // PIC 9(04)
    private BigDecimal tranCatBal;   // PIC S9(09)V99

    public TranCatBalRecord() {
        this.tranCatBal = BigDecimal.ZERO;
    }

    public long getTrancatAcctId() {
        return trancatAcctId;
    }

    public void setTrancatAcctId(long trancatAcctId) {
        this.trancatAcctId = trancatAcctId;
    }

    public String getTrancatTypeCd() {
        return trancatTypeCd;
    }

    public void setTrancatTypeCd(String trancatTypeCd) {
        this.trancatTypeCd = trancatTypeCd;
    }

    public int getTrancatCd() {
        return trancatCd;
    }

    public void setTrancatCd(int trancatCd) {
        this.trancatCd = trancatCd;
    }

    public BigDecimal getTranCatBal() {
        return tranCatBal;
    }

    public void setTranCatBal(BigDecimal tranCatBal) {
        this.tranCatBal = tranCatBal;
    }

    public String getCompositeKey() {
        return String.format("%011d%2s%04d", trancatAcctId, trancatTypeCd, trancatCd);
    }

    @Override
    public String toString() {
        return "TranCatBalRecord{" +
                "trancatAcctId=" + trancatAcctId +
                ", trancatTypeCd='" + trancatTypeCd + '\'' +
                ", trancatCd=" + trancatCd +
                ", tranCatBal=" + tranCatBal +
                '}';
    }
}
