package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Daily Transaction entity - mapped from COBOL copybook CVTRA06Y.cpy (DALYTRAN-RECORD).
 * Original sequential file: AWS.M2.CARDDEMO.DALYTRAN.PS (RECLN 350).
 * Used by batch program CBTRN02C for daily transaction posting.
 */
@Entity
@Table(name = "daily_transactions")
public class DailyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "dalytran_id", length = 16, nullable = false)
    private String dalytranId;

    @Column(name = "dalytran_type_cd", length = 2)
    private String dalytranTypeCd;

    @Column(name = "dalytran_cat_cd")
    private Integer dalytranCatCd;

    @Column(name = "dalytran_source", length = 10)
    private String dalytranSource;

    @Column(name = "dalytran_desc", length = 100)
    private String dalytranDesc;

    @Column(name = "dalytran_amt", precision = 11, scale = 2)
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

    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    public DailyTransaction() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDalytranId() {
        return dalytranId;
    }

    public void setDalytranId(String dalytranId) {
        this.dalytranId = dalytranId;
    }

    public String getDalytranTypeCd() {
        return dalytranTypeCd;
    }

    public void setDalytranTypeCd(String dalytranTypeCd) {
        this.dalytranTypeCd = dalytranTypeCd;
    }

    public Integer getDalytranCatCd() {
        return dalytranCatCd;
    }

    public void setDalytranCatCd(Integer dalytranCatCd) {
        this.dalytranCatCd = dalytranCatCd;
    }

    public String getDalytranSource() {
        return dalytranSource;
    }

    public void setDalytranSource(String dalytranSource) {
        this.dalytranSource = dalytranSource;
    }

    public String getDalytranDesc() {
        return dalytranDesc;
    }

    public void setDalytranDesc(String dalytranDesc) {
        this.dalytranDesc = dalytranDesc;
    }

    public BigDecimal getDalytranAmt() {
        return dalytranAmt;
    }

    public void setDalytranAmt(BigDecimal dalytranAmt) {
        this.dalytranAmt = dalytranAmt;
    }

    public Long getDalytranMerchantId() {
        return dalytranMerchantId;
    }

    public void setDalytranMerchantId(Long dalytranMerchantId) {
        this.dalytranMerchantId = dalytranMerchantId;
    }

    public String getDalytranMerchantName() {
        return dalytranMerchantName;
    }

    public void setDalytranMerchantName(String dalytranMerchantName) {
        this.dalytranMerchantName = dalytranMerchantName;
    }

    public String getDalytranMerchantCity() {
        return dalytranMerchantCity;
    }

    public void setDalytranMerchantCity(String dalytranMerchantCity) {
        this.dalytranMerchantCity = dalytranMerchantCity;
    }

    public String getDalytranMerchantZip() {
        return dalytranMerchantZip;
    }

    public void setDalytranMerchantZip(String dalytranMerchantZip) {
        this.dalytranMerchantZip = dalytranMerchantZip;
    }

    public String getDalytranCardNum() {
        return dalytranCardNum;
    }

    public void setDalytranCardNum(String dalytranCardNum) {
        this.dalytranCardNum = dalytranCardNum;
    }

    public String getDalytranOrigTs() {
        return dalytranOrigTs;
    }

    public void setDalytranOrigTs(String dalytranOrigTs) {
        this.dalytranOrigTs = dalytranOrigTs;
    }

    public String getDalytranProcTs() {
        return dalytranProcTs;
    }

    public void setDalytranProcTs(String dalytranProcTs) {
        this.dalytranProcTs = dalytranProcTs;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }
}
