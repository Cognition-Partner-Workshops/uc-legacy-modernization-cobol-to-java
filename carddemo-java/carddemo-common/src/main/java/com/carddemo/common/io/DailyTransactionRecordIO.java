package com.carddemo.common.io;

import com.carddemo.common.model.DailyTransactionRecord;

import static com.carddemo.common.io.FixedWidthParser.*;

/**
 * Parser and formatter for {@link DailyTransactionRecord} matching CVTRA06Y copybook layout.
 * Record length: 350 bytes. Same layout as CVTRA05Y but represents daily input transactions.
 *
 * <pre>
 * Offset  Len  Field
 * 0       16   DALYTRAN-ID              PIC X(16)
 * 16       2   DALYTRAN-TYPE-CD         PIC X(02)
 * 18       4   DALYTRAN-CAT-CD          PIC 9(04)
 * 22      10   DALYTRAN-SOURCE          PIC X(10)
 * 32     100   DALYTRAN-DESC            PIC X(100)
 * 132     11   DALYTRAN-AMT             PIC S9(09)V99 (11 bytes display)
 * 143      9   DALYTRAN-MERCHANT-ID     PIC 9(09)
 * 152     50   DALYTRAN-MERCHANT-NAME   PIC X(50)
 * 202     50   DALYTRAN-MERCHANT-CITY   PIC X(50)
 * 252     10   DALYTRAN-MERCHANT-ZIP    PIC X(10)
 * 262     16   DALYTRAN-CARD-NUM        PIC X(16)
 * 278     26   DALYTRAN-ORIG-TS         PIC X(26)
 * 304     26   DALYTRAN-PROC-TS         PIC X(26)
 * 330     20   FILLER                   PIC X(20)
 * </pre>
 */
public final class DailyTransactionRecordIO {

    /** Display width of PIC S9(09)V99: 9 integer + 2 decimal = 11 bytes (sign via overpunch). */
    private static final int SIGNED_DEC_WIDTH = 11;

    private DailyTransactionRecordIO() {}

    public static final RecordParser<DailyTransactionRecord> PARSER = line -> {
        int pos = 0;
        String tranId = parseAlpha(line, pos, 16);            pos += 16;
        String typeCd = parseAlpha(line, pos, 2);             pos += 2;
        String catCd = parseNumericString(line, pos, 4);      pos += 4;
        String source = parseAlpha(line, pos, 10);            pos += 10;
        String desc = parseAlpha(line, pos, 100);             pos += 100;
        var amt = parseSignedDecimal(line, pos, SIGNED_DEC_WIDTH, 2); pos += SIGNED_DEC_WIDTH;
        String merchantId = parseNumericString(line, pos, 9); pos += 9;
        String merchantName = parseAlpha(line, pos, 50);      pos += 50;
        String merchantCity = parseAlpha(line, pos, 50);      pos += 50;
        String merchantZip = parseAlpha(line, pos, 10);       pos += 10;
        String cardNum = parseAlpha(line, pos, 16);           pos += 16;
        String origTs = parseAlpha(line, pos, 26);            pos += 26;
        String procTs = parseAlpha(line, pos, 26);

        return new DailyTransactionRecord(tranId, typeCd, catCd, source, desc, amt,
                merchantId, merchantName, merchantCity, merchantZip,
                cardNum, origTs, procTs);
    };

    public static final RecordFormatter<DailyTransactionRecord> FORMATTER = record -> {
        StringBuilder sb = new StringBuilder(DailyTransactionRecord.RECORD_LENGTH);
        sb.append(formatAlpha(record.tranId(), 16));
        sb.append(formatAlpha(record.typeCd(), 2));
        sb.append(formatNumeric(record.catCd(), 4));
        sb.append(formatAlpha(record.source(), 10));
        sb.append(formatAlpha(record.description(), 100));
        sb.append(formatSignedDecimalFixed(record.amount(), SIGNED_DEC_WIDTH, 2));
        sb.append(formatNumeric(record.merchantId(), 9));
        sb.append(formatAlpha(record.merchantName(), 50));
        sb.append(formatAlpha(record.merchantCity(), 50));
        sb.append(formatAlpha(record.merchantZip(), 10));
        sb.append(formatAlpha(record.cardNum(), 16));
        sb.append(formatAlpha(record.origTimestamp(), 26));
        sb.append(formatAlpha(record.procTimestamp(), 26));
        sb.append(filler(20));
        return sb.toString();
    };
}
