package carddemo.records;

import carddemo.cobol.RecordArea;

/** {@code CVACT03Y} CARD-XREF-RECORD, 50 bytes, primary key XREF-CARD-NUM, alternate key XREF-ACCT-ID. */
public final class CardXrefRecord extends RecordArea {
    public static final int LENGTH = 50;
    public static final int KEY_OFFSET = 0;
    public static final int KEY_LENGTH = 16;
    public static final int ALT_KEY_OFFSET = 25;
    public static final int ALT_KEY_LENGTH = 11;

    public static final Alnum CARD_NUM = new Alnum(0, 16);
    public static final Num CUST_ID = new Num(16, 9, 0, false);
    public static final Num ACCT_ID = new Num(25, 11, 0, false);
    public static final Alnum ACCT_ID_TEXT = new Alnum(25, 11);

    public CardXrefRecord() {
        super(LENGTH);
    }

    public CardXrefRecord(byte[] image) {
        super(image);
    }
}
