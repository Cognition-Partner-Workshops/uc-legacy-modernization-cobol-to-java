package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Java equivalent of COBOL copybook CVTRA05Y - Transaction Record (RECLN 350).
 * Also maps to CVTRA06Y (Daily Transaction record) which has the same layout.
 *
 * Original COBOL layout:
 *   05 TRAN-ID              PIC X(16)
 *   05 TRAN-TYPE-CD         PIC X(02)
 *   05 TRAN-CAT-CD          PIC 9(04)
 *   05 TRAN-SOURCE          PIC X(10)
 *   05 TRAN-DESC            PIC X(100)
 *   05 TRAN-AMT             PIC S9(09)V99
 *   05 TRAN-MERCHANT-ID     PIC 9(09)
 *   05 TRAN-MERCHANT-NAME   PIC X(50)
 *   05 TRAN-MERCHANT-CITY   PIC X(50)
 *   05 TRAN-MERCHANT-ZIP    PIC X(10)
 *   05 TRAN-CARD-NUM        PIC X(16)
 *   05 TRAN-ORIG-TS         PIC X(26)
 *   05 TRAN-PROC-TS         PIC X(26)
 *   05 FILLER               PIC X(20)
 */
public class TransactionRecord {

    private String tranId;
    private String typeCd;
    private int catCd;
    private String source;
    private String description;
    private BigDecimal amount;
    private long merchantId;
    private String merchantName;
    private String merchantCity;
    private String merchantZip;
    private String cardNum;
    private String origTimestamp;
    private String procTimestamp;

    public TransactionRecord() {
        this.amount = BigDecimal.ZERO;
    }

    public String getTranId() { return tranId; }
    public void setTranId(String tranId) { this.tranId = tranId; }

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }

    public int getCatCd() { return catCd; }
    public void setCatCd(int catCd) { this.catCd = catCd; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public long getMerchantId() { return merchantId; }
    public void setMerchantId(long merchantId) { this.merchantId = merchantId; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public String getMerchantCity() { return merchantCity; }
    public void setMerchantCity(String merchantCity) { this.merchantCity = merchantCity; }

    public String getMerchantZip() { return merchantZip; }
    public void setMerchantZip(String merchantZip) { this.merchantZip = merchantZip; }

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public String getOrigTimestamp() { return origTimestamp; }
    public void setOrigTimestamp(String origTimestamp) { this.origTimestamp = origTimestamp; }

    public String getProcTimestamp() { return procTimestamp; }
    public void setProcTimestamp(String procTimestamp) { this.procTimestamp = procTimestamp; }

    @Override
    public String toString() {
        return "TransactionRecord{tranId='" + tranId + '\'' +
               ", typeCd='" + typeCd + '\'' +
               ", catCd=" + catCd +
               ", amount=" + amount +
               ", cardNum='" + cardNum + '\'' +
               '}';
    }
}
