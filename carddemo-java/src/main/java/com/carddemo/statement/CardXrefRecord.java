package com.carddemo.statement;

/** {@code CARD-XREF-RECORD} from {@code app/cpy/CVACT03Y.cpy} (RECLN 50). */
public record CardXrefRecord(String cardNumber, String customerId, String accountId) {

    public static final int RECORD_LENGTH = 50;

    public static CardXrefRecord parse(String record) {
        String rec = CobolText.alphanumeric(record, RECORD_LENGTH);
        return new CardXrefRecord(
                CobolText.field(rec, 0, 16),
                CobolText.field(rec, 16, 9),
                CobolText.field(rec, 25, 11));
    }
}
