package com.carddemo.authorization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthorizationResponse {
    private Long authId;
    private String cardNum;
    private String transactionId;
    private BigDecimal transactionAmt;
    private String authRespCode;
    private String authRespReason;
    private BigDecimal approvedAmt;
    private String status;
}
