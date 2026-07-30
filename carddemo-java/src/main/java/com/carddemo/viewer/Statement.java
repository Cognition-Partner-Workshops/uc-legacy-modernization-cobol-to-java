package com.carddemo.viewer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record Statement(
        String accountId,
        Customer customer,
        String cardNumber,
        BigDecimal currentBalance,
        int ficoScore,
        List<Transaction> transactions,
        BigDecimal totalAmount) {

    @JsonCreator
    public Statement(
            @JsonProperty("accountId") String accountId,
            @JsonProperty("customer") Customer customer,
            @JsonProperty("cardNumber") String cardNumber,
            @JsonProperty("currentBalance") BigDecimal currentBalance,
            @JsonProperty("ficoScore") int ficoScore,
            @JsonProperty("transactions") List<Transaction> transactions,
            @JsonProperty("totalAmount") BigDecimal totalAmount) {
        this.accountId = accountId;
        this.customer = customer;
        this.cardNumber = cardNumber;
        this.currentBalance = money(currentBalance);
        this.ficoScore = ficoScore;
        this.transactions = transactions == null ? List.of() : List.copyOf(transactions);
        this.totalAmount = money(totalAmount);
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP);
    }

    public record Customer(String firstName, String lastName, List<String> addressLines) {
        @JsonCreator
        public Customer(
                @JsonProperty("firstName") String firstName,
                @JsonProperty("lastName") String lastName,
                @JsonProperty("addressLines") List<String> addressLines) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.addressLines = addressLines == null ? List.of("", "", "") : List.copyOf(addressLines);
        }
    }

    public record Transaction(String transactionId, String description, BigDecimal amount) {
        @JsonCreator
        public Transaction(
                @JsonProperty("transactionId") String transactionId,
                @JsonProperty("description") String description,
                @JsonProperty("amount") BigDecimal amount) {
            this.transactionId = transactionId;
            this.description = description;
            this.amount = money(amount);
        }
    }
}
