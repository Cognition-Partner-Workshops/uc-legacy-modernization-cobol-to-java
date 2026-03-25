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
 * JPA entity mapping for the Daily Transaction VSAM record.
 * Maps to COBOL copybook: CVTRA06Y.cpy (350-byte record)
 * Same structure as Transaction but stored in a separate table/file.
 *
 * COBOL record layout:
 *   DALYTRAN-ID              PIC X(16)
 *   DALYTRAN-TYPE-CD         PIC X(02)
 *   DALYTRAN-CAT-CD          PIC 9(04)
 *   DALYTRAN-SOURCE          PIC X(10)
 *   DALYTRAN-DESC            PIC X(100)
 *   DALYTRAN-AMT             PIC S9(09)V99
 *   DALYTRAN-MERCHANT-ID     PIC 9(09)
 *   DALYTRAN-MERCHANT-NAME   PIC X(50)
 *   DALYTRAN-MERCHANT-CITY   PIC X(50)
 *   DALYTRAN-MERCHANT-ZIP    PIC X(10)
 *   DALYTRAN-CARD-NUM        PIC X(16)
 *   DALYTRAN-ORIG-TS         PIC X(26)
 *   DALYTRAN-PROC-TS         PIC X(26)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_transactions")
public class DailyTransaction {

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
