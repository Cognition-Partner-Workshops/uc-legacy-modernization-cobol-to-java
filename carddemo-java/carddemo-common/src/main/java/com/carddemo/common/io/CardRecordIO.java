package com.carddemo.common.io;

import com.carddemo.common.model.CardRecord;

import static com.carddemo.common.io.FixedWidthParser.*;

/**
 * Parser and formatter for {@link CardRecord} matching CVACT02Y copybook layout.
 * Record length: 150 bytes.
 *
 * <pre>
 * Offset  Len  Field
 * 0       16   CARD-NUM             PIC X(16)
 * 16      11   CARD-ACCT-ID         PIC 9(11)
 * 27       3   CARD-CVV-CD          PIC 9(03)
 * 30      50   CARD-EMBOSSED-NAME   PIC X(50)
 * 80      10   CARD-EXPIRAION-DATE  PIC X(10)
 * 90       1   CARD-ACTIVE-STATUS   PIC X(01)
 * 91      59   FILLER               PIC X(59)
 * </pre>
 */
public final class CardRecordIO {

    private CardRecordIO() {}

    public static final RecordParser<CardRecord> PARSER = line -> {
        int pos = 0;
        String cardNum = parseAlpha(line, pos, 16);      pos += 16;
        String acctId = parseNumericString(line, pos, 11); pos += 11;
        String cvv = parseNumericString(line, pos, 3);    pos += 3;
        String name = parseAlpha(line, pos, 50);          pos += 50;
        String expDate = parseAlpha(line, pos, 10);       pos += 10;
        String status = parseAlpha(line, pos, 1);

        return new CardRecord(cardNum, acctId, cvv, name, expDate, status);
    };

    public static final RecordFormatter<CardRecord> FORMATTER = record -> {
        StringBuilder sb = new StringBuilder(CardRecord.RECORD_LENGTH);
        sb.append(formatAlpha(record.cardNum(), 16));
        sb.append(formatNumeric(record.cardAcctId(), 11));
        sb.append(formatNumeric(record.cardCvvCd(), 3));
        sb.append(formatAlpha(record.embossedName(), 50));
        sb.append(formatAlpha(record.expirationDate(), 10));
        sb.append(formatAlpha(record.activeStatus(), 1));
        sb.append(filler(59));
        return sb.toString();
    };
}
