package com.cardemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Disclosure Group entity - converted from COBOL copybook CVTRA02Y.cpy
 * Original VSAM record length: 50 bytes
 */
@Entity
@Table(name = "disclosure_groups")
@IdClass(DisclosureGroupKey.class)
public class DisclosureGroup {

    @Id
    @Column(name = "dis_acct_group_id", length = 10)
    private String acctGroupId;

    @Id
    @Column(name = "dis_tran_type_cd", length = 2)
    private String tranTypeCd;

    @Id
    @Column(name = "dis_tran_cat_cd")
    private Integer tranCatCd;

    @Column(name = "dis_int_rate", precision = 6, scale = 2)
    private BigDecimal interestRate;

    public DisclosureGroup() {}

    public String getAcctGroupId() { return acctGroupId; }
    public void setAcctGroupId(String acctGroupId) { this.acctGroupId = acctGroupId; }

    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String tranTypeCd) { this.tranTypeCd = tranTypeCd; }

    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer tranCatCd) { this.tranCatCd = tranCatCd; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
}
