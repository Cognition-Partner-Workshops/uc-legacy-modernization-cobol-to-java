package com.carddemo.common.io;

import com.carddemo.common.model.CardXrefRecord;

import static com.carddemo.common.io.FixedWidthParser.*;

/**
 * Parser and formatter for {@link CardXrefRecord} matching CVACT03Y copybook layout.
 * Record length: 50 bytes.
 *
 * <pre>
 * Offset  Len  Field
 * 0       16   XREF-CARD-NUM   PIC X(16)
 * 16       9   XREF-CUST-ID    PIC 9(09)
 * 25      11   XREF-ACCT-ID    PIC 9(11)
 * 36      14   FILLER          PIC X(14)
 * </pre>
 */
public final class CardXrefRecordIO {

    private CardXrefRecordIO() {}

    public static final RecordParser<CardXrefRecord> PARSER = line -> {
        int pos = 0;
        String cardNum = parseAlpha(line, pos, 16);        pos += 16;
        String custId = parseNumericString(line, pos, 9);  pos += 9;
        String acctId = parseNumericString(line, pos, 11);

        return new CardXrefRecord(cardNum, custId, acctId);
    };

    public static final RecordFormatter<CardXrefRecord> FORMATTER = record -> {
        StringBuilder sb = new StringBuilder(CardXrefRecord.RECORD_LENGTH);
        sb.append(formatAlpha(record.cardNum(), 16));
        sb.append(formatNumeric(record.custId(), 9));
        sb.append(formatNumeric(record.acctId(), 11));
        sb.append(filler(14));
        return sb.toString();
    };
}
