package com.carddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Bill payment request DTO - replaces BMS map COBIL00 input fields.
 * Maps to COBOL program COBIL00C (Bill Payment, CICS txn CB00).
 */
public class BillPaymentRequest {

    @NotNull(message = "Account ID is required")
    private Long acctId;

    @NotBlank(message = "Card number is required")
    private String cardNum;

    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    private BigDecimal paymentAmt;

    public BillPaymentRequest() {
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public BigDecimal getPaymentAmt() {
        return paymentAmt;
    }

    public void setPaymentAmt(BigDecimal paymentAmt) {
        this.paymentAmt = paymentAmt;
    }
}
