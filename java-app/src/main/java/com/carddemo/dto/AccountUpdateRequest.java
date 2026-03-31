package com.carddemo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountUpdateRequest {
    private Long acctId;
    private String activeStatus;
    private BigDecimal creditLimit;
    private BigDecimal cashCreditLimit;
    private String expirationDate;
    private String reissueDate;
    private String groupId;
}
