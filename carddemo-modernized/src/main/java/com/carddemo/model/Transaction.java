package com.carddemo.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Transaction entity - modernized from COBOL copybook CVTRA05Y (TRAN-RECORD).
 *
 * Legacy COBOL layout (RECLN 350):
 *   TRAN-ID               PIC X(16)       -> transactionId
 *   TRAN-TYPE-CD           PIC X(02)       -> typeCode
 *   TRAN-CAT-CD            PIC 9(04)       -> categoryCode
 *   TRAN-SOURCE            PIC X(10)       -> source
 *   TRAN-DESC              PIC X(100)      -> description
 *   TRAN-AMT               PIC S9(09)V99   -> amount
 *   TRAN-MERCHANT-ID       PIC 9(09)       -> merchantId
 *   TRAN-MERCHANT-NAME     PIC X(50)       -> merchantName
 *   TRAN-MERCHANT-CITY     PIC X(50)       -> merchantCity
 *   TRAN-MERCHANT-ZIP      PIC X(10)       -> merchantZip
 *   TRAN-CARD-NUM          PIC X(16)       -> cardNumber
 *   TRAN-ORIG-TS           PIC X(26)       -> originTimestamp
 *   TRAN-PROC-TS           PIC X(26)       -> processedTimestamp
 *
 * Legacy data store: VSAM KSDS (TRANSACT)
 * Modernized to: Cassandra table (high-throughput, time-series data)
 */
@Table("transactions")
public class Transaction {

    @PrimaryKey("transaction_id")
    private String transactionId;

    @Column("type_code")
    private String typeCode;

    @Column("category_code")
    private Integer categoryCode;

    @Column("source")
    private String source;

    @Column("description")
    private String description;

    @Column("amount")
    private BigDecimal amount;

    @Column("merchant_id")
    private String merchantId;

    @Column("merchant_name")
    private String merchantName;

    @Column("merchant_city")
    private String merchantCity;

    @Column("merchant_zip")
    private String merchantZip;

    @Column("card_number")
    private String cardNumber;

    @Column("origin_timestamp")
    private Instant originTimestamp;

    @Column("processed_timestamp")
    private Instant processedTimestamp;

    public Transaction() {}

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public Integer getCategoryCode() { return categoryCode; }
    public void setCategoryCode(Integer categoryCode) { this.categoryCode = categoryCode; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getMerchantCity() { return merchantCity; }
    public void setMerchantCity(String merchantCity) { this.merchantCity = merchantCity; }
    public String getMerchantZip() { return merchantZip; }
    public void setMerchantZip(String merchantZip) { this.merchantZip = merchantZip; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public Instant getOriginTimestamp() { return originTimestamp; }
    public void setOriginTimestamp(Instant originTimestamp) { this.originTimestamp = originTimestamp; }
    public Instant getProcessedTimestamp() { return processedTimestamp; }
    public void setProcessedTimestamp(Instant processedTimestamp) { this.processedTimestamp = processedTimestamp; }
}
