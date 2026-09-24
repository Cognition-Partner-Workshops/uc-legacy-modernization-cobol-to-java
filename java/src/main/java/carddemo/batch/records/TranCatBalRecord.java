package carddemo.batch.records;

import carddemo.batch.io.FixedRecord;

/** CVTRA01Y TRAN-CAT-BAL-RECORD (50 bytes). Key = account id + type code + category code. */
public final class TranCatBalRecord extends FixedRecord {
    public static final int LENGTH = 50;
    public static final int KEY_OFFSET = 0;
    public static final int KEY_LENGTH = 17;

    private static final int ACCT_ID = 0;
    private static final int TYPE_CD = 11;
    private static final int CAT_CD = 13;
    private static final int BAL = 17;
    private static final int BAL_LENGTH = 11;

    public TranCatBalRecord() {
        super(LENGTH);
    }

    public static String key(String acctId, String typeCd, String catCd) {
        return acctId + typeCd + catCd;
    }

    public String key() {
        return str(KEY_OFFSET, KEY_LENGTH);
    }

    public String acctId() {
        return str(ACCT_ID, 11);
    }

    public void setAcctId(String acctId) {
        setStr(ACCT_ID, 11, acctId);
    }

    public String typeCd() {
        return str(TYPE_CD, 2);
    }

    public void setTypeCd(String typeCd) {
        setStr(TYPE_CD, 2, typeCd);
    }

    public String catCd() {
        return str(CAT_CD, 4);
    }

    public void setCatCd(String catCd) {
        setStr(CAT_CD, 4, catCd);
    }

    public long bal() {
        return num(BAL, BAL_LENGTH);
    }

    public void setBal(long cents) {
        setSigned(BAL, BAL_LENGTH, cents);
    }

    /**
     * INITIALIZE: numeric fields to unsigned zeros, alphanumeric fields to spaces; the FILLER
     * keeps whatever it held (INITIALIZE does not touch FILLER items).
     */
    public void initialize() {
        setUnsigned(ACCT_ID, 11, 0);
        setStr(TYPE_CD, 2, "");
        setUnsigned(CAT_CD, 4, 0);
        setUnsigned(BAL, BAL_LENGTH, 0);
    }
}
