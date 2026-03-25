package com.cardemo.model;

/**
 * Communication area for CardDemo application - migrated from COBOL copybook COCOM01Y.cpy.
 * Replaces the CICS COMMAREA used to pass data between COBOL programs.
 */
public class CardDemoCommarea {

    // General Info
    private String fromTranId;
    private String fromProgram;
    private String toTranId;
    private String toProgram;
    private String userId;
    private String userType; // 'A' = Admin, 'U' = User
    private int pgmContext;  // 0 = Enter, 1 = Reenter

    // Customer Info
    private long custId;
    private String custFirstName;
    private String custMiddleName;
    private String custLastName;

    // Account Info
    private long acctId;
    private String acctStatus;

    // Card Info
    private long cardNum;

    // More Info
    private String lastMap;
    private String lastMapset;

    public CardDemoCommarea() {
    }

    public boolean isAdmin() {
        return "A".equals(userType);
    }

    public boolean isRegularUser() {
        return "U".equals(userType);
    }

    public boolean isPgmEnter() {
        return pgmContext == 0;
    }

    public boolean isPgmReenter() {
        return pgmContext == 1;
    }

    // Getters and Setters
    public String getFromTranId() { return fromTranId; }
    public void setFromTranId(String fromTranId) { this.fromTranId = fromTranId; }
    public String getFromProgram() { return fromProgram; }
    public void setFromProgram(String fromProgram) { this.fromProgram = fromProgram; }
    public String getToTranId() { return toTranId; }
    public void setToTranId(String toTranId) { this.toTranId = toTranId; }
    public String getToProgram() { return toProgram; }
    public void setToProgram(String toProgram) { this.toProgram = toProgram; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public int getPgmContext() { return pgmContext; }
    public void setPgmContext(int pgmContext) { this.pgmContext = pgmContext; }
    public long getCustId() { return custId; }
    public void setCustId(long custId) { this.custId = custId; }
    public String getCustFirstName() { return custFirstName; }
    public void setCustFirstName(String custFirstName) { this.custFirstName = custFirstName; }
    public String getCustMiddleName() { return custMiddleName; }
    public void setCustMiddleName(String custMiddleName) { this.custMiddleName = custMiddleName; }
    public String getCustLastName() { return custLastName; }
    public void setCustLastName(String custLastName) { this.custLastName = custLastName; }
    public long getAcctId() { return acctId; }
    public void setAcctId(long acctId) { this.acctId = acctId; }
    public String getAcctStatus() { return acctStatus; }
    public void setAcctStatus(String acctStatus) { this.acctStatus = acctStatus; }
    public long getCardNum() { return cardNum; }
    public void setCardNum(long cardNum) { this.cardNum = cardNum; }
    public String getLastMap() { return lastMap; }
    public void setLastMap(String lastMap) { this.lastMap = lastMap; }
    public String getLastMapset() { return lastMapset; }
    public void setLastMapset(String lastMapset) { this.lastMapset = lastMapset; }
}
