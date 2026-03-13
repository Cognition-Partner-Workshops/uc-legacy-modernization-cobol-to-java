package com.carddemo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Transaction entity.
 * Maps to COBOL copybook: CVTRA05Y.cpy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private String transactionId;
    private String typeCode;
    private String categoryCode;
    private String source;
    private String description;
    private BigDecimal amount;
    private String merchantId;
    private String merchantName;
    private String merchantCity;
    private String merchantZip;
    private String cardNumber;
    private LocalDateTime originTimestamp;
    private LocalDateTime processedTimestamp;
}
