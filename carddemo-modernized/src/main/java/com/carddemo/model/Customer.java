package com.carddemo.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDate;

/**
 * Customer entity - modernized from COBOL copybook CVCUS01Y (CUSTOMER-RECORD).
 *
 * Legacy COBOL layout (RECLN 500):
 *   CUST-ID                    PIC 9(09)   -> customerId
 *   CUST-FIRST-NAME            PIC X(25)   -> firstName
 *   CUST-MIDDLE-NAME           PIC X(25)   -> middleName
 *   CUST-LAST-NAME             PIC X(25)   -> lastName
 *   CUST-ADDR-LINE-1           PIC X(50)   -> addressLine1
 *   CUST-ADDR-LINE-2           PIC X(50)   -> addressLine2
 *   CUST-ADDR-LINE-3           PIC X(50)   -> addressLine3
 *   CUST-ADDR-STATE-CD         PIC X(02)   -> stateCode
 *   CUST-ADDR-COUNTRY-CD       PIC X(03)   -> countryCode
 *   CUST-ADDR-ZIP              PIC X(10)   -> zipCode
 *   CUST-PHONE-NUM-1           PIC X(15)   -> phoneNumber1
 *   CUST-PHONE-NUM-2           PIC X(15)   -> phoneNumber2
 *   CUST-SSN                   PIC 9(09)   -> ssn
 *   CUST-GOVT-ISSUED-ID        PIC X(20)   -> governmentIssuedId
 *   CUST-DOB-YYYY-MM-DD        PIC X(10)   -> dateOfBirth
 *   CUST-EFT-ACCOUNT-ID        PIC X(10)   -> eftAccountId
 *   CUST-PRI-CARD-HOLDER-IND   PIC X(01)   -> primaryCardHolderIndicator
 *   CUST-FICO-CREDIT-SCORE     PIC 9(03)   -> ficoCreditScore
 *
 * Legacy data store: VSAM KSDS (CUSTDAT)
 * Modernized to: Cassandra table
 */
@Table("customers")
public class Customer {

    @PrimaryKey("customer_id")
    private String customerId;

    @Column("first_name")
    private String firstName;

    @Column("middle_name")
    private String middleName;

    @Column("last_name")
    private String lastName;

    @Column("address_line_1")
    private String addressLine1;

    @Column("address_line_2")
    private String addressLine2;

    @Column("address_line_3")
    private String addressLine3;

    @Column("state_code")
    private String stateCode;

    @Column("country_code")
    private String countryCode;

    @Column("zip_code")
    private String zipCode;

    @Column("phone_number_1")
    private String phoneNumber1;

    @Column("phone_number_2")
    private String phoneNumber2;

    @Column("ssn")
    private String ssn;

    @Column("government_issued_id")
    private String governmentIssuedId;

    @Column("date_of_birth")
    private LocalDate dateOfBirth;

    @Column("eft_account_id")
    private String eftAccountId;

    @Column("primary_card_holder_indicator")
    private String primaryCardHolderIndicator;

    @Column("fico_credit_score")
    private Integer ficoCreditScore;

    public Customer() {}

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
    public String getAddressLine3() { return addressLine3; }
    public void setAddressLine3(String addressLine3) { this.addressLine3 = addressLine3; }
    public String getStateCode() { return stateCode; }
    public void setStateCode(String stateCode) { this.stateCode = stateCode; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getPhoneNumber1() { return phoneNumber1; }
    public void setPhoneNumber1(String phoneNumber1) { this.phoneNumber1 = phoneNumber1; }
    public String getPhoneNumber2() { return phoneNumber2; }
    public void setPhoneNumber2(String phoneNumber2) { this.phoneNumber2 = phoneNumber2; }
    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }
    public String getGovernmentIssuedId() { return governmentIssuedId; }
    public void setGovernmentIssuedId(String governmentIssuedId) { this.governmentIssuedId = governmentIssuedId; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getEftAccountId() { return eftAccountId; }
    public void setEftAccountId(String eftAccountId) { this.eftAccountId = eftAccountId; }
    public String getPrimaryCardHolderIndicator() { return primaryCardHolderIndicator; }
    public void setPrimaryCardHolderIndicator(String primaryCardHolderIndicator) { this.primaryCardHolderIndicator = primaryCardHolderIndicator; }
    public Integer getFicoCreditScore() { return ficoCreditScore; }
    public void setFicoCreditScore(Integer ficoCreditScore) { this.ficoCreditScore = ficoCreditScore; }
}
