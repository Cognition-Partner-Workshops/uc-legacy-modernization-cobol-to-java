package com.carddemo.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity mapping for the Transaction VSAM record.
 * Maps to COBOL copybook: CVTRA05Y.cpy (350-byte record)
 *
 * COBOL record layout:
 *   TRAN-ID              PIC X(16)
 *   TRAN-TYPE-CD         PIC X(02)
 *   TRAN-CAT-CD          PIC 9(04)
 *   TRAN-SOURCE          PIC X(10)
 *   TRAN-DESC            PIC X(100)
 *   TRAN-AMT             PIC S9(09)V99
 *   TRAN-MERCHANT-ID     PIC 9(09)
 *   TRAN-MERCHANT-NAME   PIC X(50)
 *   TRAN-MERCHANT-CITY   PIC X(50)
 *   TRAN-MERCHANT-ZIP    PIC X(10)
 *   TRAN-CARD-NUM        PIC X(16)
 *   TRAN-ORIG-TS         PIC X(26)
 *   TRAN-PROC-TS         PIC X(26)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "transaction_id", length = 16, nullable = false)
    private String transactionId;

    @Column(name = "type_code", length = 2)
    private String typeCode;

    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "source", length = 10)
    private String source;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "amount", precision = 11, scale = 2)
    private BigDecimal amount;

    @Column(name = "merchant_id", length = 9)
    private String merchantId;

    @Column(name = "merchant_name", length = 50)
    private String merchantName;

    @Column(name = "merchant_city", length = 50)
    private String merchantCity;

    @Column(name = "merchant_zip", length = 10)
    private String merchantZip;

    @Column(name = "card_number", length = 16)
    private String cardNumber;

    @Column(name = "origin_timestamp")
    private LocalDateTime originTimestamp;

    @Column(name = "processed_timestamp")
    private LocalDateTime processedTimestamp;
}
