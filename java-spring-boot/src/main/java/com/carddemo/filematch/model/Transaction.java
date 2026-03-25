package com.carddemo.filematch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * JPA entity mapping the COBOL TRAN-RECORD copybook (CVTRA05Y.cpy).
 *
 * COBOL layout (RECLN 350):
 *   05 TRAN-ID               PIC X(16)
 *   05 TRAN-TYPE-CD           PIC X(02)
 *   05 TRAN-CAT-CD            PIC 9(04)
 *   05 TRAN-SOURCE            PIC X(10)
 *   05 TRAN-DESC              PIC X(100)
 *   05 TRAN-AMT               PIC S9(09)V99
 *   05 TRAN-MERCHANT-ID       PIC 9(09)
 *   05 TRAN-MERCHANT-NAME     PIC X(50)
 *   05 TRAN-MERCHANT-CITY     PIC X(50)
 *   05 TRAN-MERCHANT-ZIP      PIC X(10)
 *   05 TRAN-CARD-NUM          PIC X(16)
 *   05 TRAN-ORIG-TS           PIC X(26)
 *   05 TRAN-PROC-TS           PIC X(26)
 *   05 FILLER                 PIC X(20)
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "tran_id", length = 16)
    private String tranId;

    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    @Column(name = "tran_cat_cd")
    private Integer tranCatCd;

    @Column(name = "tran_source", length = 10)
    private String tranSource;

    @Column(name = "tran_desc", length = 100)
    private String tranDesc;

    @Column(name = "tran_amt", precision = 11, scale = 2)
    private BigDecimal tranAmt;

    @Column(name = "tran_merchant_id", length = 9)
    private String tranMerchantId;

    @Column(name = "tran_merchant_name", length = 50)
    private String tranMerchantName;

    @Column(name = "tran_merchant_city", length = 50)
    private String tranMerchantCity;

    @Column(name = "tran_merchant_zip", length = 10)
    private String tranMerchantZip;

    @Column(name = "tran_card_num", length = 16)
    private String tranCardNum;

    @Column(name = "tran_orig_ts", length = 26)
    private String tranOrigTs;

    @Column(name = "tran_proc_ts", length = 26)
    private String tranProcTs;

    public Transaction() {
    }

    public Transaction(String tranId, String tranTypeCd, Integer tranCatCd,
                       String tranSource, String tranDesc, BigDecimal tranAmt,
                       String tranMerchantId, String tranMerchantName,
                       String tranMerchantCity, String tranMerchantZip,
                       String tranCardNum, String tranOrigTs, String tranProcTs) {
        this.tranId = tranId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
        this.tranSource = tranSource;
        this.tranDesc = tranDesc;
        this.tranAmt = tranAmt;
        this.tranMerchantId = tranMerchantId;
        this.tranMerchantName = tranMerchantName;
        this.tranMerchantCity = tranMerchantCity;
        this.tranMerchantZip = tranMerchantZip;
        this.tranCardNum = tranCardNum;
        this.tranOrigTs = tranOrigTs;
        this.tranProcTs = tranProcTs;
    }

    public String getTranId() {
        return tranId;
    }

    public void setTranId(String tranId) {
        this.tranId = tranId;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public Integer getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(Integer tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    public String getTranSource() {
        return tranSource;
    }

    public void setTranSource(String tranSource) {
        this.tranSource = tranSource;
    }

    public String getTranDesc() {
        return tranDesc;
    }

    public void setTranDesc(String tranDesc) {
        this.tranDesc = tranDesc;
    }

    public BigDecimal getTranAmt() {
        return tranAmt;
    }

    public void setTranAmt(BigDecimal tranAmt) {
        this.tranAmt = tranAmt;
    }

    public String getTranMerchantId() {
        return tranMerchantId;
    }

    public void setTranMerchantId(String tranMerchantId) {
        this.tranMerchantId = tranMerchantId;
    }

    public String getTranMerchantName() {
        return tranMerchantName;
    }

    public void setTranMerchantName(String tranMerchantName) {
        this.tranMerchantName = tranMerchantName;
    }

    public String getTranMerchantCity() {
        return tranMerchantCity;
    }

    public void setTranMerchantCity(String tranMerchantCity) {
        this.tranMerchantCity = tranMerchantCity;
    }

    public String getTranMerchantZip() {
        return tranMerchantZip;
    }

    public void setTranMerchantZip(String tranMerchantZip) {
        this.tranMerchantZip = tranMerchantZip;
    }

    public String getTranCardNum() {
        return tranCardNum;
    }

    public void setTranCardNum(String tranCardNum) {
        this.tranCardNum = tranCardNum;
    }

    public String getTranOrigTs() {
        return tranOrigTs;
    }

    public void setTranOrigTs(String tranOrigTs) {
        this.tranOrigTs = tranOrigTs;
    }

    public String getTranProcTs() {
        return tranProcTs;
    }

    public void setTranProcTs(String tranProcTs) {
        this.tranProcTs = tranProcTs;
    }

    @Override
    public String toString() {
        return String.format(
                "Transaction{tranId='%s', type='%s', cat=%d, amt=%s, card='%s'}",
                tranId, tranTypeCd, tranCatCd, tranAmt, tranCardNum);
    }
}
