package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Java equivalent of COBOL copybook CVTRA02Y - Disclosure group (RECLN 50).
 * Maps to: DIS-GROUP-RECORD in COBOL.
 */
public class DisclosureGroupRecord {

    private String disAcctGroupId;   // PIC X(10)
    private String disTranTypeCd;    // PIC X(02)
    private int disTranCatCd;        // PIC 9(04)
    private BigDecimal disIntRate;   // PIC S9(04)V99

    public DisclosureGroupRecord() {
        this.disIntRate = BigDecimal.ZERO;
    }

    public String getDisAcctGroupId() {
        return disAcctGroupId;
    }

    public void setDisAcctGroupId(String disAcctGroupId) {
        this.disAcctGroupId = disAcctGroupId;
    }

    public String getDisTranTypeCd() {
        return disTranTypeCd;
    }

    public void setDisTranTypeCd(String disTranTypeCd) {
        this.disTranTypeCd = disTranTypeCd;
    }

    public int getDisTranCatCd() {
        return disTranCatCd;
    }

    public void setDisTranCatCd(int disTranCatCd) {
        this.disTranCatCd = disTranCatCd;
    }

    public BigDecimal getDisIntRate() {
        return disIntRate;
    }

    public void setDisIntRate(BigDecimal disIntRate) {
        this.disIntRate = disIntRate;
    }

    public String getCompositeKey() {
        return String.format("%-10s%2s%04d", disAcctGroupId, disTranTypeCd, disTranCatCd);
    }

    @Override
    public String toString() {
        return "DisclosureGroupRecord{" +
                "disAcctGroupId='" + disAcctGroupId + '\'' +
                ", disTranTypeCd='" + disTranTypeCd + '\'' +
                ", disTranCatCd=" + disTranCatCd +
                ", disIntRate=" + disIntRate +
                '}';
    }
}
