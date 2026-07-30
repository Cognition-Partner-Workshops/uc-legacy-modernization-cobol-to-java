package com.carddemo.statement.viewer;

import java.math.BigDecimal;
import java.util.List;

public record Statement(
        String accountId,
        Customer customer,
        String cardNumber,
        List<Transaction> transactions,
        BigDecimal totalAmount) {
    public Statement {
        transactions = List.copyOf(transactions);
    }
}
