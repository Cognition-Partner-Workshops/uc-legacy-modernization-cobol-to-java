package carddemo.records;

import carddemo.cobol.RecordArea;

/**
 * {@code CVTRA05Y} TRAN-RECORD and {@code CVTRA06Y} DALYTRAN-RECORD share this 350-byte layout.
 * Primary key TRAN-ID (bytes 1-16).
 */
public final class TranRecord extends RecordArea {
    public static final int LENGTH = 350;
    public static final int KEY_OFFSET = 0;
    public static final int KEY_LENGTH = 16;

    public static final Alnum ID = new Alnum(0, 16);
    public static final Alnum TYPE_CD = new Alnum(16, 2);
    public static final Num CAT_CD = new Num(18, 4, 0, false);
    public static final Alnum CAT_CD_TEXT = new Alnum(18, 4);
    public static final Alnum SOURCE = new Alnum(22, 10);
    public static final Alnum DESC = new Alnum(32, 100);
    public static final Num AMT = new Num(132, 11, 2, true);
    public static final Num MERCHANT_ID = new Num(143, 9, 0, false);
    public static final Alnum MERCHANT_NAME = new Alnum(152, 50);
    public static final Alnum MERCHANT_CITY = new Alnum(202, 50);
    public static final Alnum MERCHANT_ZIP = new Alnum(252, 10);
    public static final Alnum CARD_NUM = new Alnum(262, 16);
    public static final Alnum ORIG_TS = new Alnum(278, 26);
    public static final Alnum PROC_TS = new Alnum(304, 26);

    public TranRecord() {
        super(LENGTH);
    }

    public TranRecord(byte[] image) {
        super(image);
    }
}
