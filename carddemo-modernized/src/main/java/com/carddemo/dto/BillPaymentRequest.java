package com.carddemo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Bill payment request DTO - replaces COBOL COBIL00C screen input.
 * Legacy: COBIL00C program processes bill payments via BMS map COBIL00
 */
public class BillPaymentRequest {

    @NotBlank(message = "Account ID is required")
    private String accountId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    private BigDecimal amount;

    public BillPaymentRequest() {}

    public BillPaymentRequest(String accountId, BigDecimal amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
