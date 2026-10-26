package carddemo.records;

import carddemo.cobol.RecordArea;

/** {@code CVACT02Y} CARD-RECORD, 150 bytes, primary key CARD-NUM (bytes 1-16). */
public final class CardRecord extends RecordArea {
    public static final int LENGTH = 150;
    public static final int KEY_OFFSET = 0;
    public static final int KEY_LENGTH = 16;

    public static final Alnum CARD_NUM = new Alnum(0, 16);
    public static final Num CARD_ACCT_ID = new Num(16, 11, 0, false);
    public static final Num CARD_CVV_CD = new Num(27, 3, 0, false);
    public static final Alnum CARD_EMBOSSED_NAME = new Alnum(30, 50);
    public static final Alnum CARD_EXPIRAION_DATE = new Alnum(80, 10);
    public static final Alnum CARD_ACTIVE_STATUS = new Alnum(90, 1);
    public static final Alnum FILLER = new Alnum(91, 59);

    public CardRecord() {
        super(LENGTH);
    }

    public CardRecord(byte[] image) {
        super(image);
    }
}
