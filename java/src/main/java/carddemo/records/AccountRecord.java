package carddemo.records;

import carddemo.cobol.RecordArea;

/** {@code CVACT01Y} ACCOUNT-RECORD, 300 bytes, primary key ACCT-ID (bytes 1-11). */
public final class AccountRecord extends RecordArea {
    public static final int LENGTH = 300;
    public static final int KEY_OFFSET = 0;
    public static final int KEY_LENGTH = 11;

    public static final Num ACCT_ID = new Num(0, 11, 0, false);
    public static final Alnum ACCT_ID_TEXT = new Alnum(0, 11);
    public static final Alnum ACTIVE_STATUS = new Alnum(11, 1);
    public static final Num CURR_BAL = new Num(12, 12, 2, true);
    public static final Num CREDIT_LIMIT = new Num(24, 12, 2, true);
    public static final Num CASH_CREDIT_LIMIT = new Num(36, 12, 2, true);
    public static final Alnum OPEN_DATE = new Alnum(48, 10);
    public static final Alnum EXPIRATION_DATE = new Alnum(58, 10);
    public static final Alnum REISSUE_DATE = new Alnum(68, 10);
    public static final Num CURR_CYC_CREDIT = new Num(78, 12, 2, true);
    public static final Num CURR_CYC_DEBIT = new Num(90, 12, 2, true);
    public static final Alnum ADDR_ZIP = new Alnum(102, 10);
    public static final Alnum GROUP_ID = new Alnum(112, 10);

    public AccountRecord() {
        super(LENGTH);
    }

    public AccountRecord(byte[] image) {
        super(image);
    }
}
