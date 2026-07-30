package com.carddemo.statement.model;

import java.math.BigDecimal;
import java.util.List;

public record Statement(String accountId, CustomerJson customer, String cardNumber,
                        List<TransactionJson> transactions, BigDecimal totalAmount) {
  public record CustomerJson(String firstName, String lastName, List<String> addressLines) {}
  public record TransactionJson(String transactionId, String description, BigDecimal amount) {}
  public static Statement from(String accountId, Customer customer, String cardNumber,
                               List<Transaction> transactions) {
    var jsonTransactions = transactions.stream()
        .map(t -> new TransactionJson(t.transactionId(), t.description(), t.amount())).toList();
    var total = transactions.stream().map(Transaction::amount)
        .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    return new Statement(accountId,
        new CustomerJson(customer.firstName(), customer.lastName(), customer.addressLines()),
        mask(cardNumber), jsonTransactions, total);
  }
  private static String mask(String card) {
    String digits = card.replaceAll("\\D", "");
    String last4 = digits.length() >= 4 ? digits.substring(digits.length() - 4) : digits;
    return "****-****-****-" + last4;
  }
}
