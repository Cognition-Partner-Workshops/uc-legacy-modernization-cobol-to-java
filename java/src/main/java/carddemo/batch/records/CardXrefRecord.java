package carddemo.batch.records;

import carddemo.batch.io.FixedRecord;

/** CVACT03Y CARD-XREF-RECORD (50 bytes; the ASCII fixture carries the first 36). */
public final class CardXrefRecord extends FixedRecord {
    public static final int LENGTH = 50;
    public static final int CARD_NUM_OFFSET = 0;
    public static final int CARD_NUM_LENGTH = 16;
    public static final int ACCT_ID_OFFSET = 25;

    private static final int CUST_ID = 16;

    public CardXrefRecord() {
        super(LENGTH);
    }

    public String cardNum() {
        return str(CARD_NUM_OFFSET, CARD_NUM_LENGTH);
    }

    public void setCardNum(String cardNum) {
        setStr(CARD_NUM_OFFSET, CARD_NUM_LENGTH, cardNum);
    }

    public String custId() {
        return str(CUST_ID, 9);
    }

    public String acctId() {
        return str(ACCT_ID_OFFSET, 11);
    }

    public void setAcctId(String acctId) {
        setStr(ACCT_ID_OFFSET, 11, acctId);
    }
}
