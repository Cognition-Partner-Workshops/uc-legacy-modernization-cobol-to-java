package com.carddemo.authorization.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AuthorizationRequest {
    private String cardNum;
    private String transactionId;
    private BigDecimal transactionAmt;
}
