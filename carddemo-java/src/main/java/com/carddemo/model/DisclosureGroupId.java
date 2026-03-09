package com.carddemo.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for DisclosureGroup entity.
 * Maps the VSAM composite key: DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD.
 */
public class DisclosureGroupId implements Serializable {

    private String disAcctGroupId;
    private String disTranTypeCd;
    private Integer disTranCatCd;

    public DisclosureGroupId() {
    }

    public DisclosureGroupId(String disAcctGroupId, String disTranTypeCd, Integer disTranCatCd) {
        this.disAcctGroupId = disAcctGroupId;
        this.disTranTypeCd = disTranTypeCd;
        this.disTranCatCd = disTranCatCd;
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

    public Integer getDisTranCatCd() {
        return disTranCatCd;
    }

    public void setDisTranCatCd(Integer disTranCatCd) {
        this.disTranCatCd = disTranCatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisclosureGroupId that = (DisclosureGroupId) o;
        return Objects.equals(disAcctGroupId, that.disAcctGroupId)
                && Objects.equals(disTranTypeCd, that.disTranTypeCd)
                && Objects.equals(disTranCatCd, that.disTranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(disAcctGroupId, disTranTypeCd, disTranCatCd);
    }
}
