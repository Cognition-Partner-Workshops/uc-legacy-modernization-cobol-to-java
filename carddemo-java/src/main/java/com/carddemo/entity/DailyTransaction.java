package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Daily transaction staging entity - replaces DALYTRAN file (CVTRA06Y copybook).
 * Used by batch processing to stage transactions before posting to master.
 * Original COBOL record: 350 bytes with DALYTRAN-ID (X(16)) as key.
 */
@Entity
@Table(name = "daily_transactions")
public class DailyTransaction {

    @Id
    @Column(name = "transaction_id", length = 16)
    private String transactionId;

    @Column(name = "type_code", length = 2, nullable = false)
    private String typeCode;

    @Column(name = "category_code", nullable = false)
    private Integer categoryCode;

    @Column(name = "source", length = 10)
    private String source;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "amount", precision = 11, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "merchant_name", length = 50)
    private String merchantName;

    @Column(name = "merchant_city", length = 50)
    private String merchantCity;

    @Column(name = "merchant_zip", length = 10)
    private String merchantZip;

    @Column(name = "card_number", length = 16)
    private String cardNumber;

    @Column(name = "originated_ts")
    private LocalDateTime originatedTs;

    @Column(name = "processed_ts")
    private LocalDateTime processedTs;

    @Column(name = "status", length = 10)
    private String status = "PENDING";

    @Column(name = "rejection_reason", length = 100)
    private String rejectionReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public DailyTransaction() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

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
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getMerchantCity() { return merchantCity; }
    public void setMerchantCity(String merchantCity) { this.merchantCity = merchantCity; }
    public String getMerchantZip() { return merchantZip; }
    public void setMerchantZip(String merchantZip) { this.merchantZip = merchantZip; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public LocalDateTime getOriginatedTs() { return originatedTs; }
    public void setOriginatedTs(LocalDateTime originatedTs) { this.originatedTs = originatedTs; }
    public LocalDateTime getProcessedTs() { return processedTs; }
    public void setProcessedTs(LocalDateTime processedTs) { this.processedTs = processedTs; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
