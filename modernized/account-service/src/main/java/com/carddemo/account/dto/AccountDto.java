package com.carddemo.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private String acctId;
    private Character activeStatus;
    private BigDecimal currBal;
    private BigDecimal creditLimit;
    private BigDecimal cashCreditLimit;
    private String openDate;
    private String expirationDate;
    private String reissueDate;
    private BigDecimal currCycCredit;
    private BigDecimal currCycDebit;
    private String addrZip;
    private String groupId;
    private Long version;
}
