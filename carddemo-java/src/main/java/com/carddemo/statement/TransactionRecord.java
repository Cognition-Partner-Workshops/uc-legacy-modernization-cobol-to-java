package com.carddemo.statement;

import java.math.BigDecimal;

/**
 * {@code TRNX-RECORD} from {@code app/cpy/COSTM01.CPY}: the reporting layout produced by
 * the {@code CREASTMT.JCL} STEP010 SORT/OUTREC, i.e. card number first.
 */
public record TransactionRecord(String cardNumber, String transactionId, String rest) {

    public static final int RECORD_LENGTH = 350;
    public static final int REST_LENGTH = 318;

    public static TransactionRecord parse(String record) {
        String rec = CobolText.alphanumeric(record, RECORD_LENGTH);
        return new TransactionRecord(
                CobolText.field(rec, 0, 16),
                CobolText.field(rec, 16, 16),
                CobolText.field(rec, 32, REST_LENGTH));
    }

    public String typeCode() {
        return CobolText.field(rest, 0, 2);
    }

    public String categoryCode() {
        return CobolText.field(rest, 2, 4);
    }

    public String source() {
        return CobolText.field(rest, 6, 10);
    }

    public String description() {
        return CobolText.field(rest, 16, 100);
    }

    /** {@code TRNX-AMT PIC S9(09)V99}, zoned decimal with an overpunched sign. */
    public BigDecimal amount() {
        return ZonedDecimal.decode(CobolText.field(rest, 116, 11), 2);
    }

    public String merchantName() {
        return CobolText.field(rest, 136, 50);
    }
}
