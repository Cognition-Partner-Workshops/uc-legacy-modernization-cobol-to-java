package com.cardemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Daily Transaction entity - converted from COBOL copybook CVTRA06Y.cpy
 * Original VSAM record length: 350 bytes
 */
@Entity
@Table(name = "daily_transactions")
public class DailyTransaction {

    @Id
    @Column(name = "dalytran_id", length = 16)
    private String tranId;

    @Column(name = "dalytran_type_cd", length = 2)
    private String typeCd;

    @Column(name = "dalytran_cat_cd")
    private Integer catCd;

    @Column(name = "dalytran_source", length = 10)
    private String source;

    @Column(name = "dalytran_desc", length = 100)
    private String description;

    @Column(name = "dalytran_amt", precision = 11, scale = 2)
    private BigDecimal amount;

    @Column(name = "dalytran_merchant_id")
    private Long merchantId;

    @Column(name = "dalytran_merchant_name", length = 50)
    private String merchantName;

    @Column(name = "dalytran_merchant_city", length = 50)
    private String merchantCity;

    @Column(name = "dalytran_merchant_zip", length = 10)
    private String merchantZip;

    @Column(name = "dalytran_card_num", length = 16)
    private String cardNum;

    @Column(name = "dalytran_orig_ts", length = 26)
    private String origTimestamp;

    @Column(name = "dalytran_proc_ts", length = 26)
    private String procTimestamp;

    public DailyTransaction() {}

    public String getTranId() { return tranId; }
    public void setTranId(String tranId) { this.tranId = tranId; }

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }

    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

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
}
