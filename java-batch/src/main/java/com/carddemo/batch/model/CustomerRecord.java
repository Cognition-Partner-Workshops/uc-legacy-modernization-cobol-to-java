package com.carddemo.batch.model;

/**
 * Java equivalent of COBOL copybook CUSTREC - Customer Record (RECLN 500).
 *
 * Original COBOL layout:
 *   05 CUST-ID                  PIC 9(09)
 *   05 CUST-FIRST-NAME          PIC X(25)
 *   05 CUST-MIDDLE-NAME         PIC X(25)
 *   05 CUST-LAST-NAME           PIC X(25)
 *   05 CUST-ADDR-LINE-1         PIC X(50)
 *   05 CUST-ADDR-LINE-2         PIC X(50)
 *   05 CUST-ADDR-LINE-3         PIC X(50)
 *   05 CUST-ADDR-STATE-CD       PIC X(02)
 *   05 CUST-ADDR-COUNTRY-CD     PIC X(03)
 *   05 CUST-ADDR-ZIP            PIC X(10)
 *   05 CUST-PHONE-NUM-1         PIC X(15)
 *   05 CUST-PHONE-NUM-2         PIC X(15)
 *   05 CUST-SSN                 PIC 9(09)
 *   05 CUST-GOVT-ISSUED-ID      PIC X(20)
 *   05 CUST-DOB-YYYYMMDD        PIC X(10)
 *   05 CUST-EFT-ACCOUNT-ID      PIC X(10)
 *   05 CUST-PRI-CARD-HOLDER-IND PIC X(01)
 *   05 CUST-FICO-CREDIT-SCORE   PIC 9(03)
 *   05 FILLER                   PIC X(168)
 */
public class CustomerRecord {

    private long custId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String addrLine1;
    private String addrLine2;
    private String addrLine3;
    private String addrStateCd;
    private String addrCountryCd;
    private String addrZip;
    private String phoneNum1;
    private String phoneNum2;
    private long ssn;
    private String govtIssuedId;
    private String dobYyyymmdd;
    private String eftAccountId;
    private String priCardHolderInd;
    private int ficoCreditScore;

    public long getCustId() { return custId; }
    public void setCustId(long custId) { this.custId = custId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAddrLine1() { return addrLine1; }
    public void setAddrLine1(String addrLine1) { this.addrLine1 = addrLine1; }

    public String getAddrLine2() { return addrLine2; }
    public void setAddrLine2(String addrLine2) { this.addrLine2 = addrLine2; }

    public String getAddrLine3() { return addrLine3; }
    public void setAddrLine3(String addrLine3) { this.addrLine3 = addrLine3; }

    public String getAddrStateCd() { return addrStateCd; }
    public void setAddrStateCd(String addrStateCd) { this.addrStateCd = addrStateCd; }

    public String getAddrCountryCd() { return addrCountryCd; }
    public void setAddrCountryCd(String addrCountryCd) { this.addrCountryCd = addrCountryCd; }

    public String getAddrZip() { return addrZip; }
    public void setAddrZip(String addrZip) { this.addrZip = addrZip; }

    public String getPhoneNum1() { return phoneNum1; }
    public void setPhoneNum1(String phoneNum1) { this.phoneNum1 = phoneNum1; }

    public String getPhoneNum2() { return phoneNum2; }
    public void setPhoneNum2(String phoneNum2) { this.phoneNum2 = phoneNum2; }

    public long getSsn() { return ssn; }
    public void setSsn(long ssn) { this.ssn = ssn; }

    public String getGovtIssuedId() { return govtIssuedId; }
    public void setGovtIssuedId(String govtIssuedId) { this.govtIssuedId = govtIssuedId; }

    public String getDobYyyymmdd() { return dobYyyymmdd; }
    public void setDobYyyymmdd(String dobYyyymmdd) { this.dobYyyymmdd = dobYyyymmdd; }

    public String getEftAccountId() { return eftAccountId; }
    public void setEftAccountId(String eftAccountId) { this.eftAccountId = eftAccountId; }

    public String getPriCardHolderInd() { return priCardHolderInd; }
    public void setPriCardHolderInd(String priCardHolderInd) { this.priCardHolderInd = priCardHolderInd; }

    public int getFicoCreditScore() { return ficoCreditScore; }
    public void setFicoCreditScore(int ficoCreditScore) { this.ficoCreditScore = ficoCreditScore; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) {
            sb.append(firstName.trim());
        }
        if (middleName != null && !middleName.isBlank()) {
            sb.append(' ').append(middleName.trim());
        }
        if (lastName != null && !lastName.isBlank()) {
            sb.append(' ').append(lastName.trim());
        }
        return sb.toString().trim();
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (addrLine3 != null && !addrLine3.isBlank()) {
            sb.append(addrLine3.trim());
        }
        if (addrStateCd != null && !addrStateCd.isBlank()) {
            sb.append(' ').append(addrStateCd.trim());
        }
        if (addrCountryCd != null && !addrCountryCd.isBlank()) {
            sb.append(' ').append(addrCountryCd.trim());
        }
        if (addrZip != null && !addrZip.isBlank()) {
            sb.append(' ').append(addrZip.trim());
        }
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return "CustomerRecord{custId=" + custId +
               ", name='" + getFullName() + '\'' +
               ", ficoCreditScore=" + ficoCreditScore +
               '}';
    }
}
