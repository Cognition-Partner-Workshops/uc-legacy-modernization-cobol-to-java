package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Mirrors CVACT03Y copybook (CARD-XREF-RECORD).
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @Column(name = "xref_card_num", length = 16)
    private String xrefCardNum;

    @Column(name = "xref_cust_id")
    private Long xrefCustId;

    @Column(name = "xref_acct_id")
    private Long xrefAcctId;

    public CardXref() {}

    public String getXrefCardNum() { return xrefCardNum; }
    public void setXrefCardNum(String xrefCardNum) { this.xrefCardNum = xrefCardNum; }

    public Long getXrefCustId() { return xrefCustId; }
    public void setXrefCustId(Long xrefCustId) { this.xrefCustId = xrefCustId; }

    public Long getXrefAcctId() { return xrefAcctId; }
    public void setXrefAcctId(Long xrefAcctId) { this.xrefAcctId = xrefAcctId; }
}
