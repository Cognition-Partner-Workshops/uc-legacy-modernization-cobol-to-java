package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Card-Account Cross Reference entity - mapped from COBOL copybook CVACT03Y.cpy (CARD-XREF-RECORD).
 * Original VSAM dataset: AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS (RECLN 50).
 * Primary key: XREF-CARD-NUM PIC X(16).
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @Column(name = "xref_card_num", length = 16)
    private String xrefCardNum;

    @Column(name = "xref_cust_id", nullable = false)
    private Long xrefCustId;

    @Column(name = "xref_acct_id", nullable = false)
    private Long xrefAcctId;

    public CardXref() {
    }

    public String getXrefCardNum() {
        return xrefCardNum;
    }

    public void setXrefCardNum(String xrefCardNum) {
        this.xrefCardNum = xrefCardNum;
    }

    public Long getXrefCustId() {
        return xrefCustId;
    }

    public void setXrefCustId(Long xrefCustId) {
        this.xrefCustId = xrefCustId;
    }

    public Long getXrefAcctId() {
        return xrefAcctId;
    }

    public void setXrefAcctId(Long xrefAcctId) {
        this.xrefAcctId = xrefAcctId;
    }
}
