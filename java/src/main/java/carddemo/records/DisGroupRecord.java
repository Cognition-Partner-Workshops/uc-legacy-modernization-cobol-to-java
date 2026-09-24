package carddemo.records;

import carddemo.cobol.RecordArea;

/** {@code CVTRA02Y} DIS-GROUP-RECORD, 50 bytes, primary key DIS-GROUP-KEY (bytes 1-16). */
public final class DisGroupRecord extends RecordArea {
    public static final int LENGTH = 50;
    public static final int KEY_OFFSET = 0;
    public static final int KEY_LENGTH = 16;

    public static final Alnum ACCT_GROUP_ID = new Alnum(0, 10);
    public static final Alnum TRAN_TYPE_CD = new Alnum(10, 2);
    public static final Num TRAN_CAT_CD = new Num(12, 4, 0, false);
    public static final Alnum TRAN_CAT_CD_TEXT = new Alnum(12, 4);
    public static final Num INT_RATE = new Num(16, 6, 2, true);

    public DisGroupRecord() {
        super(LENGTH);
    }

    public DisGroupRecord(byte[] image) {
        super(image);
    }

    public byte[] key() {
        return group(KEY_OFFSET, KEY_LENGTH);
    }
}
