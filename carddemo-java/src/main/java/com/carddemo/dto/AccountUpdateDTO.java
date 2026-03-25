package com.carddemo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO for account update operations - replaces COACTUPC's extensive field validation.
 * Bean Validation annotations consolidate ~2000 lines of COBOL validation logic.
 */
public class AccountUpdateDTO {

    @Pattern(regexp = "[YN]", message = "Active status must be 'Y' or 'N'")
    private String activeStatus;

    @Positive(message = "Credit limit must be positive")
    private BigDecimal creditLimit;

    @Positive(message = "Cash credit limit must be positive")
    private BigDecimal cashCreditLimit;

    private String expirationDate;

    private String reissueDate;

    @Size(max = 10, message = "Zip code must be at most 10 characters")
    private String zipCode;

    @Size(max = 10, message = "Group ID must be at most 10 characters")
    private String groupId;

    // Associated customer update fields
    @Size(max = 25) private String firstName;
    @Size(max = 25) private String middleName;
    @Size(max = 25) private String lastName;
    @Size(max = 50) private String addrLine1;
    @Size(max = 50) private String addrLine2;
    @Size(max = 50) private String addrLine3;
    @Size(max = 2) private String stateCode;
    @Size(max = 3) private String countryCode;
    @Size(max = 15) private String phone1;
    @Size(max = 15) private String phone2;
    @Pattern(regexp = "\\d{9}", message = "SSN must be exactly 9 digits")
    private String ssn;
    @Size(max = 20) private String govtId;
    private String dateOfBirth;
    @Size(max = 10) private String eftAccountId;

    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public BigDecimal getCashCreditLimit() { return cashCreditLimit; }
    public void setCashCreditLimit(BigDecimal cashCreditLimit) { this.cashCreditLimit = cashCreditLimit; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    public String getReissueDate() { return reissueDate; }
    public void setReissueDate(String reissueDate) { this.reissueDate = reissueDate; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
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
    public String getStateCode() { return stateCode; }
    public void setStateCode(String stateCode) { this.stateCode = stateCode; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getPhone1() { return phone1; }
    public void setPhone1(String phone1) { this.phone1 = phone1; }
    public String getPhone2() { return phone2; }
    public void setPhone2(String phone2) { this.phone2 = phone2; }
    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }
    public String getGovtId() { return govtId; }
    public void setGovtId(String govtId) { this.govtId = govtId; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getEftAccountId() { return eftAccountId; }
    public void setEftAccountId(String eftAccountId) { this.eftAccountId = eftAccountId; }
}
