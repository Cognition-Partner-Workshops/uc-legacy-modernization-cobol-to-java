package com.cardemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Card Cross-Reference entity - migrated from COBOL copybook CVACT03Y.cpy.
 * Original COBOL record length: 50 bytes.
 */
@Entity
@Table(name = "card_xref")
public class CardXrefRecord {

    @Id
    @Column(name = "xref_card_num", length = 16)
    private String xrefCardNum;

    @Column(name = "xref_cust_id")
    private long xrefCustId;

    @Column(name = "xref_acct_id")
    private long xrefAcctId;

    public CardXrefRecord() {
    }

    public String getXrefCardNum() {
        return xrefCardNum;
    }

    public void setXrefCardNum(String xrefCardNum) {
        this.xrefCardNum = xrefCardNum;
    }

    public long getXrefCustId() {
        return xrefCustId;
    }

    public void setXrefCustId(long xrefCustId) {
        this.xrefCustId = xrefCustId;
    }

    public long getXrefAcctId() {
        return xrefAcctId;
    }

    public void setXrefAcctId(long xrefAcctId) {
        this.xrefAcctId = xrefAcctId;
    }

    @Override
    public String toString() {
        return "CardXrefRecord{" +
                "xrefCardNum='" + xrefCardNum + '\'' +
                ", xrefCustId=" + xrefCustId +
                ", xrefAcctId=" + xrefAcctId +
                '}';
    }
}
