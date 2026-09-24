package carddemo.batch.records;

import carddemo.batch.io.FixedRecord;

/** CVTRA02Y DIS-GROUP-RECORD (50 bytes). Key = group id + type code + category code. */
public final class DisGroupRecord extends FixedRecord {
    public static final int LENGTH = 50;
    public static final int KEY_OFFSET = 0;
    public static final int KEY_LENGTH = 16;

    private static final int GROUP_ID = 0;
    private static final int TYPE_CD = 10;
    private static final int CAT_CD = 12;
    private static final int INT_RATE = 16;
    private static final int INT_RATE_LENGTH = 6;

    public DisGroupRecord() {
        super(LENGTH);
    }

    public static String key(String groupId, String typeCd, String catCd) {
        return groupId + typeCd + catCd;
    }

    public String groupId() {
        return str(GROUP_ID, 10);
    }

    public void setGroupId(String groupId) {
        setStr(GROUP_ID, 10, groupId);
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

    /** PIC S9(04)V99, scaled by 100. */
    public long intRate() {
        return num(INT_RATE, INT_RATE_LENGTH);
    }

    public void setIntRate(long scaled) {
        setSigned(INT_RATE, INT_RATE_LENGTH, scaled);
    }
}
