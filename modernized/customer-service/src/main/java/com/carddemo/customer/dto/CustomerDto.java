package com.carddemo.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {
    private String custId;
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
    private String ssn;
    private String govtIssuedId;
    private LocalDate dob;
    private String eftAccountId;
    private String priCardHolderInd;
    private Integer ficoCreditScore;

    public static String maskSsn(String ssn) {
        if (ssn == null || ssn.length() < 4) {
            return ssn;
        }
        return "***-**-" + ssn.substring(ssn.length() - 4);
    }
}
