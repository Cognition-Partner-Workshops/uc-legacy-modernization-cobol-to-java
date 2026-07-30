package com.carddemo.statement;

import java.math.BigDecimal;
import java.util.List;

public record Statement(String accountId, CustomerDetails customer, String cardNumber,
                        BigDecimal currentBalance, int ficoScore,
                        List<TransactionDetails> transactions, BigDecimal totalAmount) {
    public record CustomerDetails(String firstName, String lastName, List<String> addressLines) {}
    public record TransactionDetails(String transactionId, String description, BigDecimal amount) {}
}
