package com.carddemo.common.model;

import java.math.BigDecimal;

/**
 * Maps COBOL copybook CVTRA06Y - Daily transaction record (RECLN 350).
 *
 * <pre>
 * 05  DALYTRAN-ID                PIC X(16)
 * 05  DALYTRAN-TYPE-CD           PIC X(02)
 * 05  DALYTRAN-CAT-CD            PIC 9(04)
 * 05  DALYTRAN-SOURCE            PIC X(10)
 * 05  DALYTRAN-DESC              PIC X(100)
 * 05  DALYTRAN-AMT               PIC S9(09)V99
 * 05  DALYTRAN-MERCHANT-ID       PIC 9(09)
 * 05  DALYTRAN-MERCHANT-NAME     PIC X(50)
 * 05  DALYTRAN-MERCHANT-CITY     PIC X(50)
 * 05  DALYTRAN-MERCHANT-ZIP      PIC X(10)
 * 05  DALYTRAN-CARD-NUM          PIC X(16)
 * 05  DALYTRAN-ORIG-TS           PIC X(26)
 * 05  DALYTRAN-PROC-TS           PIC X(26)
 * 05  FILLER                     PIC X(20)
 * </pre>
 */
public record DailyTransactionRecord(
        String tranId,           // PIC X(16)
        String typeCd,           // PIC X(02)
        String catCd,            // PIC 9(04)
        String source,           // PIC X(10)
        String description,      // PIC X(100)
        BigDecimal amount,       // PIC S9(09)V99
        String merchantId,       // PIC 9(09)
        String merchantName,     // PIC X(50)
        String merchantCity,     // PIC X(50)
        String merchantZip,      // PIC X(10)
        String cardNum,          // PIC X(16)
        String origTimestamp,    // PIC X(26)
        String procTimestamp     // PIC X(26)
) {
    public static final int RECORD_LENGTH = 350;
    public static final int TRAN_ID_LEN = 16;
    public static final int TYPE_CD_LEN = 2;
    public static final int CAT_CD_LEN = 4;
    public static final int SOURCE_LEN = 10;
    public static final int DESC_LEN = 100;
    public static final int AMT_LEN = 11; // S9(09)V99 display
    public static final int MERCHANT_ID_LEN = 9;
    public static final int MERCHANT_NAME_LEN = 50;
    public static final int MERCHANT_CITY_LEN = 50;
    public static final int MERCHANT_ZIP_LEN = 10;
    public static final int CARD_NUM_LEN = 16;
    public static final int TIMESTAMP_LEN = 26;
    public static final int FILLER_LEN = 20;
}
