package com.carddemo.statement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StatementGenerator {
    private StatementGenerator() {}

    public static List<Statement> generate(List<String> xrefLines, List<String> customerLines,
                                            List<String> accountLines, List<String> transactionLines) {
        Map<String, Customer> customers = new HashMap<>();
        customerLines.forEach(line -> {
            Customer customer = RecordParsers.customer(line);
            customers.put(customer.id(), customer);
        });
        Map<String, Account> accounts = new HashMap<>();
        accountLines.forEach(line -> {
            Account account = RecordParsers.account(line);
            accounts.put(account.id(), account);
        });
        Map<String, List<Transaction>> transactions = TransactionGrouper.group(transactionLines);
        List<Statement> statements = new ArrayList<>();
        for (String line : xrefLines) {
            Xref xref = RecordParsers.xref(line);
            Customer customer = customers.get(xref.customerId());
            if (customer == null) {
                throw new IllegalStateException("Missing customer " + xref.customerId()
                        + " for account " + xref.accountId());
            }
            Account account = accounts.get(xref.accountId());
            if (account == null) {
                throw new IllegalStateException("Missing account " + xref.accountId()
                        + " for customer " + xref.customerId());
            }
            List<Transaction> source = transactions.getOrDefault(xref.cardNumber(), List.of());
            List<Statement.TransactionDetails> details = source.stream()
                    .map(t -> new Statement.TransactionDetails(t.transactionId(), t.description(),
                            t.amount().setScale(2, RoundingMode.HALF_UP))).toList();
            BigDecimal total = details.stream().map(Statement.TransactionDetails::amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
            String address3 = String.join(" ", List.of(customer.address3(), customer.state(),
                    customer.country(), customer.zip()).stream().filter(s -> !s.isEmpty()).toList());
            statements.add(new Statement(account.id(),
                    new Statement.CustomerDetails(customer.firstName(), customer.lastName(),
                            List.of(customer.address1(), customer.address2(), address3)),
                    maskCard(xref.cardNumber()), account.currentBalance().setScale(2, RoundingMode.HALF_UP),
                    customer.ficoScore(), details, total));
        }
        return statements;
    }

    public static String maskCard(String cardNumber) {
        String value = cardNumber.trim();
        if (value.length() < 4) throw new IllegalArgumentException("Card number must have four digits");
        return "**-**-****-" + value.substring(value.length() - 4);
    }
}
