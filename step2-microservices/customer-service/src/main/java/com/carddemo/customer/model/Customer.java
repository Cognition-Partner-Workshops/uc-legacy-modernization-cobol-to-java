package com.carddemo.customer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @Column(name = "cust_id")
    private Long custId;

    @Column(name = "cust_first_name", length = 25)
    private String custFirstName;

    @Column(name = "cust_middle_name", length = 25)
    private String custMiddleName;

    @Column(name = "cust_last_name", length = 25)
    private String custLastName;

    @Column(name = "cust_addr_line_1", length = 50)
    private String custAddrLine1;

    @Column(name = "cust_addr_line_2", length = 50)
    private String custAddrLine2;

    @Column(name = "cust_addr_line_3", length = 50)
    private String custAddrLine3;

    @Column(name = "cust_addr_state_cd", length = 2)
    private String custAddrStateCd;

    @Column(name = "cust_addr_country_cd", length = 3)
    private String custAddrCountryCd;

    @Column(name = "cust_addr_zip", length = 10)
    private String custAddrZip;

    @Column(name = "cust_phone_num_1", length = 15)
    private String custPhoneNum1;

    @Column(name = "cust_phone_num_2", length = 15)
    private String custPhoneNum2;

    @Column(name = "cust_ssn", length = 9)
    private String custSsn;

    @Column(name = "cust_govt_issued_id", length = 20)
    private String custGovtIssuedId;

    @Column(name = "cust_dob_yyyy_mm_dd", length = 10)
    private String custDobYyyyMmDd;

    @Column(name = "cust_eft_account_id", length = 10)
    private String custEftAccountId;

    @Column(name = "cust_pri_card_holder_ind", length = 1)
    private String custPriCardHolderInd;

    @Column(name = "cust_fico_credit_score")
    private Short custFicoCreditScore;

    public Customer() {}

    public Long getCustId() { return custId; }
    public void setCustId(Long custId) { this.custId = custId; }
    public String getCustFirstName() { return custFirstName; }
    public void setCustFirstName(String s) { this.custFirstName = s; }
    public String getCustMiddleName() { return custMiddleName; }
    public void setCustMiddleName(String s) { this.custMiddleName = s; }
    public String getCustLastName() { return custLastName; }
    public void setCustLastName(String s) { this.custLastName = s; }
    public String getCustAddrLine1() { return custAddrLine1; }
    public void setCustAddrLine1(String s) { this.custAddrLine1 = s; }
    public String getCustAddrLine2() { return custAddrLine2; }
    public void setCustAddrLine2(String s) { this.custAddrLine2 = s; }
    public String getCustAddrLine3() { return custAddrLine3; }
    public void setCustAddrLine3(String s) { this.custAddrLine3 = s; }
    public String getCustAddrStateCd() { return custAddrStateCd; }
    public void setCustAddrStateCd(String s) { this.custAddrStateCd = s; }
    public String getCustAddrCountryCd() { return custAddrCountryCd; }
    public void setCustAddrCountryCd(String s) { this.custAddrCountryCd = s; }
    public String getCustAddrZip() { return custAddrZip; }
    public void setCustAddrZip(String s) { this.custAddrZip = s; }
    public String getCustPhoneNum1() { return custPhoneNum1; }
    public void setCustPhoneNum1(String s) { this.custPhoneNum1 = s; }
    public String getCustPhoneNum2() { return custPhoneNum2; }
    public void setCustPhoneNum2(String s) { this.custPhoneNum2 = s; }
    public String getCustSsn() { return custSsn; }
    public void setCustSsn(String s) { this.custSsn = s; }
    public String getCustGovtIssuedId() { return custGovtIssuedId; }
    public void setCustGovtIssuedId(String s) { this.custGovtIssuedId = s; }
    public String getCustDobYyyyMmDd() { return custDobYyyyMmDd; }
    public void setCustDobYyyyMmDd(String s) { this.custDobYyyyMmDd = s; }
    public String getCustEftAccountId() { return custEftAccountId; }
    public void setCustEftAccountId(String s) { this.custEftAccountId = s; }
    public String getCustPriCardHolderInd() { return custPriCardHolderInd; }
    public void setCustPriCardHolderInd(String s) { this.custPriCardHolderInd = s; }
    public Short getCustFicoCreditScore() { return custFicoCreditScore; }
    public void setCustFicoCreditScore(Short s) { this.custFicoCreditScore = s; }
}
