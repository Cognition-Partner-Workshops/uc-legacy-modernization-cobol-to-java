package carddemo.batch.records;

import carddemo.batch.io.FixedRecord;

/** CVACT01Y ACCOUNT-RECORD (300 bytes). Amounts are in cents (PIC S9(10)V99). */
public final class AccountRecord extends FixedRecord {
    public static final int LENGTH = 300;
    public static final int ID_OFFSET = 0;
    public static final int ID_LENGTH = 11;
    public static final int AMOUNT_LENGTH = 12;

    private static final int ACTIVE_STATUS = 11;
    private static final int CURR_BAL = 12;
    private static final int CREDIT_LIMIT = 24;
    private static final int CASH_CREDIT_LIMIT = 36;
    private static final int OPEN_DATE = 48;
    private static final int EXPIRATION_DATE = 58;
    private static final int REISSUE_DATE = 68;
    private static final int CURR_CYC_CREDIT = 78;
    private static final int CURR_CYC_DEBIT = 90;
    private static final int ADDR_ZIP = 102;
    private static final int GROUP_ID = 112;

    public AccountRecord() {
        super(LENGTH);
    }

    public String id() {
        return str(ID_OFFSET, ID_LENGTH);
    }

    public void setId(String id) {
        setStr(ID_OFFSET, ID_LENGTH, id);
    }

    public String activeStatus() {
        return str(ACTIVE_STATUS, 1);
    }

    public long currBal() {
        return num(CURR_BAL, AMOUNT_LENGTH);
    }

    public void setCurrBal(long cents) {
        setSigned(CURR_BAL, AMOUNT_LENGTH, cents);
    }

    public long creditLimit() {
        return num(CREDIT_LIMIT, AMOUNT_LENGTH);
    }

    public void setCreditLimit(long cents) {
        setSigned(CREDIT_LIMIT, AMOUNT_LENGTH, cents);
    }

    public long cashCreditLimit() {
        return num(CASH_CREDIT_LIMIT, AMOUNT_LENGTH);
    }

    public String openDate() {
        return str(OPEN_DATE, 10);
    }

    public String expirationDate() {
        return str(EXPIRATION_DATE, 10);
    }

    public void setExpirationDate(String date) {
        setStr(EXPIRATION_DATE, 10, date);
    }

    public String reissueDate() {
        return str(REISSUE_DATE, 10);
    }

    public long currCycCredit() {
        return num(CURR_CYC_CREDIT, AMOUNT_LENGTH);
    }

    public void setCurrCycCredit(long cents) {
        setSigned(CURR_CYC_CREDIT, AMOUNT_LENGTH, cents);
    }

    public long currCycDebit() {
        return num(CURR_CYC_DEBIT, AMOUNT_LENGTH);
    }

    public void setCurrCycDebit(long cents) {
        setSigned(CURR_CYC_DEBIT, AMOUNT_LENGTH, cents);
    }

    public String addrZip() {
        return str(ADDR_ZIP, 10);
    }

    public String groupId() {
        return str(GROUP_ID, 10);
    }

    public void setGroupId(String groupId) {
        setStr(GROUP_ID, 10, groupId);
    }

    /** Raw bytes of the fields CBACT01C copies into its output records. */
    public byte[] currBalBytes() {
        return slice(CURR_BAL, AMOUNT_LENGTH);
    }

    public byte[] creditLimitBytes() {
        return slice(CREDIT_LIMIT, AMOUNT_LENGTH);
    }

    public byte[] cashCreditLimitBytes() {
        return slice(CASH_CREDIT_LIMIT, AMOUNT_LENGTH);
    }

    public byte[] currCycCreditBytes() {
        return slice(CURR_CYC_CREDIT, AMOUNT_LENGTH);
    }
}
