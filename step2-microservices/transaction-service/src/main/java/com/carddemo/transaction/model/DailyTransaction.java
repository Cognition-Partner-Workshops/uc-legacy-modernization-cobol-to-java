package com.carddemo.transaction.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "daily_transaction")
public class DailyTransaction {

    @Id
    @Column(name = "dalytran_id", length = 16)
    private String dalytranId;

    @Column(name = "dalytran_type_cd", length = 2)
    private String dalytranTypeCd;

    @Column(name = "dalytran_cat_cd")
    private Integer dalytranCatCd;

    @Column(name = "dalytran_source", length = 10)
    private String dalytranSource;

    @Column(name = "dalytran_desc", length = 100)
    private String dalytranDesc;

    @Column(name = "dalytran_amt", precision = 12, scale = 2)
    private BigDecimal dalytranAmt;

    @Column(name = "dalytran_merchant_id")
    private Long dalytranMerchantId;

    @Column(name = "dalytran_merchant_name", length = 50)
    private String dalytranMerchantName;

    @Column(name = "dalytran_merchant_city", length = 50)
    private String dalytranMerchantCity;

    @Column(name = "dalytran_merchant_zip", length = 10)
    private String dalytranMerchantZip;

    @Column(name = "dalytran_card_num", length = 16)
    private String dalytranCardNum;

    @Column(name = "dalytran_orig_ts", length = 26)
    private String dalytranOrigTs;

    @Column(name = "dalytran_proc_ts", length = 26)
    private String dalytranProcTs;

    public DailyTransaction() {}

    public String getDalytranId() { return dalytranId; }
    public void setDalytranId(String id) { this.dalytranId = id; }
    public String getDalytranTypeCd() { return dalytranTypeCd; }
    public void setDalytranTypeCd(String s) { this.dalytranTypeCd = s; }
    public Integer getDalytranCatCd() { return dalytranCatCd; }
    public void setDalytranCatCd(Integer i) { this.dalytranCatCd = i; }
    public String getDalytranSource() { return dalytranSource; }
    public void setDalytranSource(String s) { this.dalytranSource = s; }
    public String getDalytranDesc() { return dalytranDesc; }
    public void setDalytranDesc(String s) { this.dalytranDesc = s; }
    public BigDecimal getDalytranAmt() { return dalytranAmt; }
    public void setDalytranAmt(BigDecimal v) { this.dalytranAmt = v; }
    public Long getDalytranMerchantId() { return dalytranMerchantId; }
    public void setDalytranMerchantId(Long l) { this.dalytranMerchantId = l; }
    public String getDalytranMerchantName() { return dalytranMerchantName; }
    public void setDalytranMerchantName(String s) { this.dalytranMerchantName = s; }
    public String getDalytranMerchantCity() { return dalytranMerchantCity; }
    public void setDalytranMerchantCity(String s) { this.dalytranMerchantCity = s; }
    public String getDalytranMerchantZip() { return dalytranMerchantZip; }
    public void setDalytranMerchantZip(String s) { this.dalytranMerchantZip = s; }
    public String getDalytranCardNum() { return dalytranCardNum; }
    public void setDalytranCardNum(String s) { this.dalytranCardNum = s; }
    public String getDalytranOrigTs() { return dalytranOrigTs; }
    public void setDalytranOrigTs(String s) { this.dalytranOrigTs = s; }
    public String getDalytranProcTs() { return dalytranProcTs; }
    public void setDalytranProcTs(String s) { this.dalytranProcTs = s; }
}
