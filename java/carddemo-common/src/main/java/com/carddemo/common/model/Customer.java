package com.carddemo.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * JPA entity mapping for the Customer VSAM record.
 * Maps to COBOL copybook: CVCUS01Y.cpy (500-byte record)
 * Referenced in: COACTUPC.cbl (lines 434-456)
 *
 * COBOL record layout:
 *   CUST-ID                    PIC 9(09)
 *   CUST-FIRST-NAME            PIC X(25)
 *   CUST-MIDDLE-NAME           PIC X(25)
 *   CUST-LAST-NAME             PIC X(25)
 *   CUST-ADDR-LINE-1           PIC X(50)
 *   CUST-ADDR-LINE-2           PIC X(50)
 *   CUST-ADDR-LINE-3           PIC X(50)
 *   CUST-ADDR-STATE-CD         PIC X(02)
 *   CUST-ADDR-COUNTRY-CD       PIC X(03)
 *   CUST-ADDR-ZIP              PIC X(10)
 *   CUST-PHONE-NUM-1           PIC X(15)
 *   CUST-PHONE-NUM-2           PIC X(15)
 *   CUST-SSN                   PIC 9(09)
 *   CUST-GOVT-ISSUED-ID        PIC X(20)
 *   CUST-DOB-YYYY-MM-DD        PIC X(10)
 *   CUST-EFT-ACCOUNT-ID        PIC X(10)
 *   CUST-PRI-CARD-HOLDER-IND   PIC X(01)
 *   CUST-FICO-CREDIT-SCORE     PIC 9(03)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

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

    @Column(name = "zip_code", length = 10)
    private String zipCode;

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

    @Column(name = "primary_card_holder_indicator", length = 1)
    private String primaryCardHolderIndicator;

    @Column(name = "fico_credit_score")
    private int ficoCreditScore;
}
