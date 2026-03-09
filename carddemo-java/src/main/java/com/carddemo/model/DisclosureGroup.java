package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Disclosure Group entity - mapped from COBOL copybook CVTRA02Y.cpy (DISCGRP-RECORD).
 * Original VSAM dataset: AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS (RECLN 50).
 * Composite key: DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD.
 * Used by batch program CBACT04C for interest rate lookups.
 */
@Entity
@Table(name = "disclosure_groups")
@IdClass(DisclosureGroupId.class)
public class DisclosureGroup {

    @Id
    @Column(name = "dis_acct_group_id", length = 10)
    private String disAcctGroupId;

    @Id
    @Column(name = "dis_tran_type_cd", length = 2)
    private String disTranTypeCd;

    @Id
    @Column(name = "dis_tran_cat_cd")
    private Integer disTranCatCd;

    @Column(name = "dis_int_rate", precision = 7, scale = 4, nullable = false)
    private BigDecimal disIntRate = BigDecimal.ZERO;

    @Column(name = "dis_fee_amt", precision = 9, scale = 2, nullable = false)
    private BigDecimal disFeeAmt = BigDecimal.ZERO;

    public DisclosureGroup() {
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

    public BigDecimal getDisIntRate() {
        return disIntRate;
    }

    public void setDisIntRate(BigDecimal disIntRate) {
        this.disIntRate = disIntRate;
    }

    public BigDecimal getDisFeeAmt() {
        return disFeeAmt;
    }

    public void setDisFeeAmt(BigDecimal disFeeAmt) {
        this.disFeeAmt = disFeeAmt;
    }
}
