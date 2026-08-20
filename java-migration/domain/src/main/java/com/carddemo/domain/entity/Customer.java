package com.carddemo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @Column(name = "cust_id", precision = 9, scale = 0)
    private BigDecimal custId;
    @Column(name = "first_name", length = 25)
    private String firstName;
    @Column(name = "middle_name", length = 25)
    private String middleName;
    @Column(name = "last_name", length = 25)
    private String lastName;
    @Column(name = "addr_line_1", length = 50)
    private String addrLine1;
    @Column(name = "addr_line_2", length = 50)
    private String addrLine2;
    @Column(name = "addr_line_3", length = 50)
    private String addrLine3;
    @Column(name = "addr_state_cd", length = 2)
    private String addrStateCd;
    @Column(name = "addr_country_cd", length = 3)
    private String addrCountryCd;
    @Column(name = "addr_zip", length = 10)
    private String addrZip;
    @Column(name = "phone_num_1", length = 15)
    private String phoneNum1;
    @Column(name = "phone_num_2", length = 15)
    private String phoneNum2;
    @Column(name = "ssn", precision = 9, scale = 0)
    private BigDecimal ssn;
    @Column(name = "govt_issued_id", length = 20)
    private String govtIssuedId;
    @Column(name = "dob_yyyy_mm_dd", length = 10)
    private String dobYyyyMmDd;
    @Column(name = "eft_account_id", length = 10)
    private String eftAccountId;
    @Column(name = "pri_card_holder_ind", length = 1)
    private String priCardHolderInd;
    @Column(name = "fico_credit_score", precision = 3, scale = 0)
    private BigDecimal ficoCreditScore;

    protected Customer() {
    }

    public BigDecimal getCustId() { return custId; }
    public void setCustId(BigDecimal custId) { this.custId = custId; }
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
    public BigDecimal getSsn() { return ssn; }
    public void setSsn(BigDecimal ssn) { this.ssn = ssn; }
    public String getGovtIssuedId() { return govtIssuedId; }
    public void setGovtIssuedId(String govtIssuedId) { this.govtIssuedId = govtIssuedId; }
    public String getDobYyyyMmDd() { return dobYyyyMmDd; }
    public void setDobYyyyMmDd(String dobYyyyMmDd) { this.dobYyyyMmDd = dobYyyyMmDd; }
    public String getEftAccountId() { return eftAccountId; }
    public void setEftAccountId(String eftAccountId) { this.eftAccountId = eftAccountId; }
    public String getPriCardHolderInd() { return priCardHolderInd; }
    public void setPriCardHolderInd(String priCardHolderInd) { this.priCardHolderInd = priCardHolderInd; }
    public BigDecimal getFicoCreditScore() { return ficoCreditScore; }
    public void setFicoCreditScore(BigDecimal ficoCreditScore) { this.ficoCreditScore = ficoCreditScore; }
}
