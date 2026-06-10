package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Size(max = 16)
    @Column(name = "tran_id", length = 16)
    private String tranId;

    @Size(max = 2)
    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCode;

    @Column(name = "tran_cat_cd")
    private int tranCategoryCode;

    @Size(max = 10)
    @Column(name = "tran_source", length = 10)
    private String tranSource;

    @Size(max = 100)
    @Column(name = "tran_desc", length = 100)
    private String tranDescription;

    @Column(name = "tran_amt", precision = 11, scale = 2)
    private BigDecimal tranAmount;

    @Column(name = "merchant_id")
    private long merchantId;

    @Size(max = 50)
    @Column(name = "merchant_name", length = 50)
    private String merchantName;

    @Size(max = 50)
    @Column(name = "merchant_city", length = 50)
    private String merchantCity;

    @Size(max = 10)
    @Column(name = "merchant_zip", length = 10)
    private String merchantZip;

    @NotNull
    @Size(max = 16)
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNum;

    @Size(max = 26)
    @Column(name = "orig_ts", length = 26)
    private String originTimestamp;

    @Size(max = 26)
    @Column(name = "proc_ts", length = 26)
    private String processedTimestamp;

    @Column(name = "posted", nullable = false)
    private boolean posted = false;

    public Transaction() {}

    public String getTranId() { return tranId; }
    public void setTranId(String tranId) { this.tranId = tranId; }

    public String getTranTypeCode() { return tranTypeCode; }
    public void setTranTypeCode(String tranTypeCode) { this.tranTypeCode = tranTypeCode; }

    public int getTranCategoryCode() { return tranCategoryCode; }
    public void setTranCategoryCode(int tranCategoryCode) { this.tranCategoryCode = tranCategoryCode; }

    public String getTranSource() { return tranSource; }
    public void setTranSource(String tranSource) { this.tranSource = tranSource; }

    public String getTranDescription() { return tranDescription; }
    public void setTranDescription(String tranDescription) { this.tranDescription = tranDescription; }

    public BigDecimal getTranAmount() { return tranAmount; }
    public void setTranAmount(BigDecimal tranAmount) { this.tranAmount = tranAmount; }

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

    public String getOriginTimestamp() { return originTimestamp; }
    public void setOriginTimestamp(String originTimestamp) { this.originTimestamp = originTimestamp; }

    public String getProcessedTimestamp() { return processedTimestamp; }
    public void setProcessedTimestamp(String processedTimestamp) { this.processedTimestamp = processedTimestamp; }

    public boolean isPosted() { return posted; }
    public void setPosted(boolean posted) { this.posted = posted; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(tranId, that.tranId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tranId);
    }
}
