package carddemo.records;

import carddemo.cobol.RecordArea;

/** {@code CVTRA01Y} TRAN-CAT-BAL-RECORD, 50 bytes, primary key TRAN-CAT-KEY (bytes 1-17). */
public final class TranCatBalRecord extends RecordArea {
    public static final int LENGTH = 50;
    public static final int KEY_OFFSET = 0;
    public static final int KEY_LENGTH = 17;

    public static final Num ACCT_ID = new Num(0, 11, 0, false);
    public static final Alnum ACCT_ID_TEXT = new Alnum(0, 11);
    public static final Alnum TYPE_CD = new Alnum(11, 2);
    public static final Num CAT_CD = new Num(13, 4, 0, false);
    public static final Alnum CAT_CD_TEXT = new Alnum(13, 4);
    public static final Num BAL = new Num(17, 11, 2, true);

    public TranCatBalRecord() {
        super(LENGTH);
    }

    public TranCatBalRecord(byte[] image) {
        super(image);
    }

    /** COBOL {@code INITIALIZE}: numerics to zero, alphanumerics to spaces, FILLER untouched. */
    public void initialize() {
        initialize(ACCT_ID);
        initialize(TYPE_CD);
        initialize(CAT_CD);
        initialize(BAL);
    }

    public byte[] key() {
        return group(KEY_OFFSET, KEY_LENGTH);
    }
}
