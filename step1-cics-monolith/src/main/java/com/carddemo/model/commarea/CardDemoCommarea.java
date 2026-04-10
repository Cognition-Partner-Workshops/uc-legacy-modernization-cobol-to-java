package com.carddemo.model.commarea;

import java.io.Serializable;

/**
 * Exact field-for-field translation of COCOM01Y.cpy (CARDDEMO-COMMAREA).
 * Stored in HttpSession under key "CARDDEMO_COMMAREA".
 */
public class CardDemoCommarea implements Serializable {

    private static final long serialVersionUID = 1L;

    // CDEMO-GENERAL-INFO
    private String fromTranId = "";       // CDEMO-FROM-TRANID PIC X(04)
    private String fromProgram = "";      // CDEMO-FROM-PROGRAM PIC X(08)
    private String toTranId = "";         // CDEMO-TO-TRANID PIC X(04)
    private String toProgram = "";        // CDEMO-TO-PROGRAM PIC X(08)
    private String userId = "";           // CDEMO-USER-ID PIC X(08)
    private String userType = "";         // CDEMO-USER-TYPE PIC X(01) 'A' or 'U'
    private int pgmContext = 0;           // CDEMO-PGM-CONTEXT PIC 9(01) 0=ENTER, 1=REENTER

    // CDEMO-CUSTOMER-INFO
    private long custId = 0;             // CDEMO-CUST-ID PIC 9(09)
    private String custFirstName = "";   // CDEMO-CUST-FNAME PIC X(25)
    private String custMiddleName = "";  // CDEMO-CUST-MNAME PIC X(25)
    private String custLastName = "";    // CDEMO-CUST-LNAME PIC X(25)

    // CDEMO-ACCOUNT-INFO
    private long acctId = 0;            // CDEMO-ACCT-ID PIC 9(11)
    private String acctStatus = "";     // CDEMO-ACCT-STATUS PIC X(01)

    // CDEMO-CARD-INFO
    private String cardNum = "";         // CDEMO-CARD-NUM PIC 9(16)

    // CDEMO-MORE-INFO
    private String lastMap = "";         // CDEMO-LAST-MAP PIC X(7)
    private String lastMapset = "";      // CDEMO-LAST-MAPSET PIC X(7)

    // CT02-specific extended fields for Transaction Add
    private String ct02TrnIdFirst = "";
    private String ct02TrnIdLast = "";
    private int ct02PageNum = 0;
    private String ct02NextPageFlg = "N";
    private String ct02TrnSelFlg = "";
    private String ct02TrnSelected = "";

    public boolean isAdmin() {
        return "A".equals(userType);
    }

    public boolean isUser() {
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

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public String getLastMap() { return lastMap; }
    public void setLastMap(String lastMap) { this.lastMap = lastMap; }

    public String getLastMapset() { return lastMapset; }
    public void setLastMapset(String lastMapset) { this.lastMapset = lastMapset; }

    public String getCt02TrnIdFirst() { return ct02TrnIdFirst; }
    public void setCt02TrnIdFirst(String ct02TrnIdFirst) { this.ct02TrnIdFirst = ct02TrnIdFirst; }

    public String getCt02TrnIdLast() { return ct02TrnIdLast; }
    public void setCt02TrnIdLast(String ct02TrnIdLast) { this.ct02TrnIdLast = ct02TrnIdLast; }

    public int getCt02PageNum() { return ct02PageNum; }
    public void setCt02PageNum(int ct02PageNum) { this.ct02PageNum = ct02PageNum; }

    public String getCt02NextPageFlg() { return ct02NextPageFlg; }
    public void setCt02NextPageFlg(String ct02NextPageFlg) { this.ct02NextPageFlg = ct02NextPageFlg; }

    public String getCt02TrnSelFlg() { return ct02TrnSelFlg; }
    public void setCt02TrnSelFlg(String ct02TrnSelFlg) { this.ct02TrnSelFlg = ct02TrnSelFlg; }

    public String getCt02TrnSelected() { return ct02TrnSelected; }
    public void setCt02TrnSelected(String ct02TrnSelected) { this.ct02TrnSelected = ct02TrnSelected; }
}
