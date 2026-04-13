package com.carddemo.dto;

import java.math.BigDecimal;

public class PaymentForm {
    private Long acctId;
    private BigDecimal amount;

    public PaymentForm() {}

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
