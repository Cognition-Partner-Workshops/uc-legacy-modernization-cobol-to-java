package carddemo.batch.records;

import carddemo.batch.io.FixedRecord;

/**
 * CVTRA05Y TRAN-RECORD / CVTRA06Y DALYTRAN-RECORD (350 bytes, identical layouts).
 * Amounts are in cents (PIC S9(09)V99).
 */
public final class TranRecord extends FixedRecord {
    public static final int LENGTH = 350;
    public static final int ID_OFFSET = 0;
    public static final int ID_LENGTH = 16;

    private static final int TYPE_CD = 16;
    private static final int CAT_CD = 18;
    private static final int SOURCE = 22;
    private static final int DESC = 32;
    private static final int AMT = 132;
    private static final int AMT_LENGTH = 11;
    private static final int MERCHANT_ID = 143;
    private static final int MERCHANT_NAME = 152;
    private static final int MERCHANT_CITY = 202;
    private static final int MERCHANT_ZIP = 252;
    private static final int CARD_NUM = 262;
    private static final int ORIG_TS = 278;
    private static final int PROC_TS = 304;

    public TranRecord() {
        super(LENGTH);
    }

    public String id() {
        return str(ID_OFFSET, ID_LENGTH);
    }

    public void setId(String id) {
        setStr(ID_OFFSET, ID_LENGTH, id);
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

    public String source() {
        return str(SOURCE, 10);
    }

    public void setSource(String source) {
        setStr(SOURCE, 10, source);
    }

    public String desc() {
        return str(DESC, 100);
    }

    public void setDesc(String desc) {
        setStr(DESC, 100, desc);
    }

    public long amt() {
        return num(AMT, AMT_LENGTH);
    }

    public void setAmt(long cents) {
        setSigned(AMT, AMT_LENGTH, cents);
    }

    public String merchantId() {
        return str(MERCHANT_ID, 9);
    }

    public void setMerchantId(long merchantId) {
        setUnsigned(MERCHANT_ID, 9, merchantId);
    }

    public void setMerchantId(String merchantId) {
        setStr(MERCHANT_ID, 9, merchantId);
    }

    public String merchantName() {
        return str(MERCHANT_NAME, 50);
    }

    public void setMerchantName(String name) {
        setStr(MERCHANT_NAME, 50, name);
    }

    public String merchantCity() {
        return str(MERCHANT_CITY, 50);
    }

    public void setMerchantCity(String city) {
        setStr(MERCHANT_CITY, 50, city);
    }

    public String merchantZip() {
        return str(MERCHANT_ZIP, 10);
    }

    public void setMerchantZip(String zip) {
        setStr(MERCHANT_ZIP, 10, zip);
    }

    public String cardNum() {
        return str(CARD_NUM, 16);
    }

    public void setCardNum(String cardNum) {
        setStr(CARD_NUM, 16, cardNum);
    }

    public String origTs() {
        return str(ORIG_TS, 26);
    }

    public void setOrigTs(String ts) {
        setStr(ORIG_TS, 26, ts);
    }

    public String procTs() {
        return str(PROC_TS, 26);
    }

    public void setProcTs(String ts) {
        setStr(PROC_TS, 26, ts);
    }
}
