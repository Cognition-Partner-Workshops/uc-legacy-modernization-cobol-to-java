package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Java equivalent of COBOL copybook CVTRA05Y - Transaction record (RECLN 350).
 * Maps to: TRAN-RECORD in COBOL.
 * Also used for CVTRA06Y (DALYTRAN-RECORD) which has the same structure.
 */
public class TransactionRecord {

    private String tranId;            // PIC X(16)
    private String tranTypeCd;        // PIC X(02)
    private int tranCatCd;            // PIC 9(04)
    private String tranSource;        // PIC X(10)
    private String tranDesc;          // PIC X(100)
    private BigDecimal tranAmt;       // PIC S9(09)V99
    private long tranMerchantId;      // PIC 9(09)
    private String tranMerchantName;  // PIC X(50)
    private String tranMerchantCity;  // PIC X(50)
    private String tranMerchantZip;   // PIC X(10)
    private String tranCardNum;       // PIC X(16)
    private String tranOrigTs;        // PIC X(26)
    private String tranProcTs;        // PIC X(26)

    public TransactionRecord() {
        this.tranAmt = BigDecimal.ZERO;
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

    public int getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(int tranCatCd) {
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

    public long getTranMerchantId() {
        return tranMerchantId;
    }

    public void setTranMerchantId(long tranMerchantId) {
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
        return "TransactionRecord{" +
                "tranId='" + tranId + '\'' +
                ", tranTypeCd='" + tranTypeCd + '\'' +
                ", tranCatCd=" + tranCatCd +
                ", tranSource='" + tranSource + '\'' +
                ", tranDesc='" + tranDesc + '\'' +
                ", tranAmt=" + tranAmt +
                ", tranCardNum='" + tranCardNum + '\'' +
                '}';
    }
}
