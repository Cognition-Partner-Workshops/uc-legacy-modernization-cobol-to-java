package com.carddemo.customer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * JPA entity mapping the COBOL CUSTOMER-RECORD from copybook CVCUS01Y.cpy.
 *
 * <pre>
 * COBOL Field                  -> Java Field
 * CUST-ID           PIC 9(09)  -> id (Long)
 * CUST-FIRST-NAME   PIC X(25)  -> firstName (String)
 * CUST-MIDDLE-NAME  PIC X(25)  -> middleName (String)
 * CUST-LAST-NAME    PIC X(25)  -> lastName (String)
 * CUST-ADDR-LINE-1  PIC X(50)  -> addressLine1 (String)
 * CUST-ADDR-LINE-2  PIC X(50)  -> addressLine2 (String)
 * CUST-ADDR-LINE-3  PIC X(50)  -> addressLine3 (String)
 * CUST-ADDR-STATE-CD PIC X(02) -> stateCode (String)
 * CUST-ADDR-COUNTRY-CD PIC X(03) -> countryCode (String)
 * CUST-ADDR-ZIP     PIC X(10)  -> zip (String)
 * CUST-PHONE-NUM-1  PIC X(15)  -> phoneNumber1 (String)
 * CUST-PHONE-NUM-2  PIC X(15)  -> phoneNumber2 (String)
 * CUST-SSN          PIC 9(09)  -> ssn (String)
 * CUST-GOVT-ISSUED-ID PIC X(20) -> govtIssuedId (String)
 * CUST-DOB-YYYY-MM-DD PIC X(10) -> dateOfBirth (LocalDate)
 * CUST-EFT-ACCOUNT-ID PIC X(10) -> eftAccountId (String)
 * CUST-PRI-CARD-HOLDER-IND PIC X(01) -> primaryCardHolderInd (String)
 * CUST-FICO-CREDIT-SCORE PIC 9(03) -> ficoCreditScore (Integer)
 * </pre>
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @Column(name = "cust_id")
    private Long id;

    @Column(name = "first_name", length = 25)
    private String firstName;

    @Column(name = "middle_name", length = 25)
    private String middleName;

    @Column(name = "last_name", length = 25)
    private String lastName;

    @Column(name = "address_line_1", length = 50)
    private String addressLine1;

    @Column(name = "address_line_2", length = 50)
    private String addressLine2;

    @Column(name = "address_line_3", length = 50)
    private String addressLine3;

    @Column(name = "state_code", length = 2)
    private String stateCode;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(name = "zip", length = 10)
    private String zip;

    @Column(name = "phone_number_1", length = 15)
    private String phoneNumber1;

    @Column(name = "phone_number_2", length = 15)
    private String phoneNumber2;

    @Column(name = "ssn", length = 9)
    private String ssn;

    @Column(name = "govt_issued_id", length = 20)
    private String govtIssuedId;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "eft_account_id", length = 10)
    private String eftAccountId;

    @Column(name = "primary_card_holder_ind", length = 1)
    private String primaryCardHolderInd;

    @Column(name = "fico_credit_score")
    private Integer ficoCreditScore;

    public Customer() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPhoneNumber1() {
        return phoneNumber1;
    }

    public void setPhoneNumber1(String phoneNumber1) {
        this.phoneNumber1 = phoneNumber1;
    }

    public String getPhoneNumber2() {
        return phoneNumber2;
    }

    public void setPhoneNumber2(String phoneNumber2) {
        this.phoneNumber2 = phoneNumber2;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getGovtIssuedId() {
        return govtIssuedId;
    }

    public void setGovtIssuedId(String govtIssuedId) {
        this.govtIssuedId = govtIssuedId;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEftAccountId() {
        return eftAccountId;
    }

    public void setEftAccountId(String eftAccountId) {
        this.eftAccountId = eftAccountId;
    }

    public String getPrimaryCardHolderInd() {
        return primaryCardHolderInd;
    }

    public void setPrimaryCardHolderInd(String primaryCardHolderInd) {
        this.primaryCardHolderInd = primaryCardHolderInd;
    }

    public Integer getFicoCreditScore() {
        return ficoCreditScore;
    }

    public void setFicoCreditScore(Integer ficoCreditScore) {
        this.ficoCreditScore = ficoCreditScore;
    }
}
