package com.carddemo.customer.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCustomerRequest {
    @Size(max = 25)
    private String firstName;

    @Size(max = 25)
    private String middleName;

    @Size(max = 25)
    private String lastName;

    @Size(max = 50)
    private String addrLine1;
    @Size(max = 50)
    private String addrLine2;
    @Size(max = 50)
    private String addrLine3;

    @Size(max = 2)
    private String addrStateCd;
    @Size(max = 3)
    private String addrCountryCd;
    @Size(max = 10)
    private String addrZip;

    @Size(max = 15)
    private String phoneNum1;
    @Size(max = 15)
    private String phoneNum2;

    @Pattern(regexp = "\\d{9}", message = "SSN must be exactly 9 digits")
    private String ssn;

    @Size(max = 20)
    private String govtIssuedId;

    private LocalDate dob;

    @Size(max = 10)
    private String eftAccountId;

    @Size(max = 1)
    private String priCardHolderInd;

    private Integer ficoCreditScore;
}
