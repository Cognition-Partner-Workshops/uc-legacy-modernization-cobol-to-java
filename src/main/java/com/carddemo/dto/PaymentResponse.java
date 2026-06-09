package com.carddemo.dto;

import java.math.BigDecimal;

public record PaymentResponse(
        String transactionId,
        String cardNum,
        BigDecimal amount,
        String status,
        String timestamp
) {}
