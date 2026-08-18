package com.carddemo.parity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** One account entry of the frozen parity JSON contract. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountParity(
        String accountId,
        String customerName,
        int transactionCount,
        BigDecimal totalAmount,
        String cobolTextPath,
        String cobolHtmlPath,
        String javaTextPath,
        String javaHtmlPath,
        ParityStatus status,
        int diffLineCount,
        Integer firstDiffLine) {

    public ParityStatus status() {
        return status == null ? ParityStatus.MISSING : status;
    }

    public BigDecimal totalAmount() {
        return totalAmount == null ? BigDecimal.ZERO : totalAmount;
    }
}
