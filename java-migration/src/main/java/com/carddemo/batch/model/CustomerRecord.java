package com.carddemo.batch.model;

/**
 * Java equivalent of COBOL copybook CVCUS01Y - Customer entity (RECLN 500).
 * Maps to: CUSTOMER-RECORD in COBOL.
 */
public class CustomerRecord {

    private long custId;                  // PIC 9(09)
    private String custFirstName;         // PIC X(25)
    private String custMiddleName;        // PIC X(25)
    private String custLastName;          // PIC X(25)
    private String custAddrLine1;         // PIC X(50)
    private String custAddrLine2;         // PIC X(50)
    private String custAddrLine3;         // PIC X(50)
    private String custAddrStateCd;       // PIC X(02)
    private String custAddrCountryCd;     // PIC X(03)
    private String custAddrZip;           // PIC X(10)
    private String custPhoneNum1;         // PIC X(15)
    private String custPhoneNum2;         // PIC X(15)
    private long custSsn;                 // PIC 9(09)
    private String custGovtIssuedId;      // PIC X(20)
    private String custDobYyyyMmDd;       // PIC X(10)
    private String custEftAccountId;      // PIC X(10)
    private String custPriCardHolderInd;  // PIC X(01)
    private int custFicoCreditScore;      // PIC 9(03)

    public long getCustId() {
        return custId;
    }

    public void setCustId(long custId) {
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

    public String getCustAddrLine1() {
        return custAddrLine1;
    }

    public void setCustAddrLine1(String custAddrLine1) {
        this.custAddrLine1 = custAddrLine1;
    }

    public String getCustAddrLine2() {
        return custAddrLine2;
    }

    public void setCustAddrLine2(String custAddrLine2) {
        this.custAddrLine2 = custAddrLine2;
    }

    public String getCustAddrLine3() {
        return custAddrLine3;
    }

    public void setCustAddrLine3(String custAddrLine3) {
        this.custAddrLine3 = custAddrLine3;
    }

    public String getCustAddrStateCd() {
        return custAddrStateCd;
    }

    public void setCustAddrStateCd(String custAddrStateCd) {
        this.custAddrStateCd = custAddrStateCd;
    }

    public String getCustAddrCountryCd() {
        return custAddrCountryCd;
    }

    public void setCustAddrCountryCd(String custAddrCountryCd) {
        this.custAddrCountryCd = custAddrCountryCd;
    }

    public String getCustAddrZip() {
        return custAddrZip;
    }

    public void setCustAddrZip(String custAddrZip) {
        this.custAddrZip = custAddrZip;
    }

    public String getCustPhoneNum1() {
        return custPhoneNum1;
    }

    public void setCustPhoneNum1(String custPhoneNum1) {
        this.custPhoneNum1 = custPhoneNum1;
    }

    public String getCustPhoneNum2() {
        return custPhoneNum2;
    }

    public void setCustPhoneNum2(String custPhoneNum2) {
        this.custPhoneNum2 = custPhoneNum2;
    }

    public long getCustSsn() {
        return custSsn;
    }

    public void setCustSsn(long custSsn) {
        this.custSsn = custSsn;
    }

    public String getCustGovtIssuedId() {
        return custGovtIssuedId;
    }

    public void setCustGovtIssuedId(String custGovtIssuedId) {
        this.custGovtIssuedId = custGovtIssuedId;
    }

    public String getCustDobYyyyMmDd() {
        return custDobYyyyMmDd;
    }

    public void setCustDobYyyyMmDd(String custDobYyyyMmDd) {
        this.custDobYyyyMmDd = custDobYyyyMmDd;
    }

    public String getCustEftAccountId() {
        return custEftAccountId;
    }

    public void setCustEftAccountId(String custEftAccountId) {
        this.custEftAccountId = custEftAccountId;
    }

    public String getCustPriCardHolderInd() {
        return custPriCardHolderInd;
    }

    public void setCustPriCardHolderInd(String custPriCardHolderInd) {
        this.custPriCardHolderInd = custPriCardHolderInd;
    }

    public int getCustFicoCreditScore() {
        return custFicoCreditScore;
    }

    public void setCustFicoCreditScore(int custFicoCreditScore) {
        this.custFicoCreditScore = custFicoCreditScore;
    }

    @Override
    public String toString() {
        return "CustomerRecord{" +
                "custId=" + custId +
                ", custFirstName='" + custFirstName + '\'' +
                ", custMiddleName='" + custMiddleName + '\'' +
                ", custLastName='" + custLastName + '\'' +
                ", custAddrLine1='" + custAddrLine1 + '\'' +
                ", custAddrStateCd='" + custAddrStateCd + '\'' +
                ", custAddrZip='" + custAddrZip + '\'' +
                ", custFicoCreditScore=" + custFicoCreditScore +
                '}';
    }
}
