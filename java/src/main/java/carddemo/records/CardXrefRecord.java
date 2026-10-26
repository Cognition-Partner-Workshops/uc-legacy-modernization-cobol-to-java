package carddemo.records;

import carddemo.cobol.RecordArea;

/** {@code CVACT03Y} CARD-XREF-RECORD, 50 bytes, primary key XREF-CARD-NUM (bytes 1-16). */
public final class CardXrefRecord extends RecordArea {
    public static final int LENGTH = 50;
    public static final int KEY_OFFSET = 0;
    public static final int KEY_LENGTH = 16;

    public static final Alnum XREF_CARD_NUM = new Alnum(0, 16);
    public static final Num XREF_CUST_ID = new Num(16, 9, 0, false);
    public static final Num XREF_ACCT_ID = new Num(25, 11, 0, false);
    public static final Alnum FILLER = new Alnum(36, 14);

    public CardXrefRecord() {
        super(LENGTH);
    }

    public CardXrefRecord(byte[] image) {
        super(image);
    }
}
