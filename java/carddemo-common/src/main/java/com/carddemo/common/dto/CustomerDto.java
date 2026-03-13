package com.carddemo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object for Customer entity.
 * Maps to COBOL copybook: CVCUS01Y.cpy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long customerId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String stateCode;
    private String countryCode;
    private String zipCode;
    private String phoneNumber1;
    private String phoneNumber2;
    private String ssn;
    private String govtIssuedId;
    private LocalDate dateOfBirth;
    private String eftAccountId;
    private String primaryCardHolderIndicator;
    private int ficoCreditScore;
}
