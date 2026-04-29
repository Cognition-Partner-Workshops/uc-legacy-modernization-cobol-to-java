package com.cardemo.common.dto;

import com.cardemo.common.enums.UserType;
import java.io.Serializable;

/**
 * Session DTO mapped from COBOL copybook COCOM01Y.cpy (CARDDEMO-COMMAREA).
 * Source: app/cpy/COCOM01Y.cpy
 *
 * Replaces the CICS COMMAREA used for inter-program communication.
 * Stored in HttpSession to maintain state across pseudo-conversational requests.
 *
 * TODO: Implement session timeout handling equivalent to CICS task timeout
 */
public class CardDemoSession implements Serializable {

    private String fromTranId;
    private String fromProgram;
    private String toTranId;
    private String toProgram;
    private String userId;
    private UserType userType;
    private int pgmContext;

    private Long custId;
    private String custFirstName;
    private String custMiddleName;
    private String custLastName;

    private Long acctId;
    private String acctStatus;

    private Long cardNum;

    private String lastMap;
    private String lastMapset;

    public CardDemoSession() {
    }

    public String getFromTranId() {
        return fromTranId;
    }

    public void setFromTranId(String fromTranId) {
        this.fromTranId = fromTranId;
    }

    public String getFromProgram() {
        return fromProgram;
    }

    public void setFromProgram(String fromProgram) {
        this.fromProgram = fromProgram;
    }

    public String getToTranId() {
        return toTranId;
    }

    public void setToTranId(String toTranId) {
        this.toTranId = toTranId;
    }

    public String getToProgram() {
        return toProgram;
    }

    public void setToProgram(String toProgram) {
        this.toProgram = toProgram;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public int getPgmContext() {
        return pgmContext;
    }

    public void setPgmContext(int pgmContext) {
        this.pgmContext = pgmContext;
    }

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public String getCustFirstName() {
        return custFirstName;
    }

    public void setCustFirstName(String custFirstName) {
        this.custFirstName = custFirstName;
    }

    public String getCustMiddleName() {
        return custMiddleName;
    }

    public void setCustMiddleName(String custMiddleName) {
        this.custMiddleName = custMiddleName;
    }

    public String getCustLastName() {
        return custLastName;
    }

    public void setCustLastName(String custLastName) {
        this.custLastName = custLastName;
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public String getAcctStatus() {
        return acctStatus;
    }

    public void setAcctStatus(String acctStatus) {
        this.acctStatus = acctStatus;
    }

    public Long getCardNum() {
        return cardNum;
    }

    public void setCardNum(Long cardNum) {
        this.cardNum = cardNum;
    }

    public String getLastMap() {
        return lastMap;
    }

    public void setLastMap(String lastMap) {
        this.lastMap = lastMap;
    }

    public String getLastMapset() {
        return lastMapset;
    }

    public void setLastMapset(String lastMapset) {
        this.lastMapset = lastMapset;
    }
}
