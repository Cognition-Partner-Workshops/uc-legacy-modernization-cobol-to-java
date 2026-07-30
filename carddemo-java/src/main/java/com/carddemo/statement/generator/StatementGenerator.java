package com.carddemo.statement.generator;

import com.carddemo.statement.model.*;
import com.carddemo.statement.parser.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public final class StatementGenerator {
  private final CardXrefParser xrefParser = new CardXrefParser();
  private final CustomerParser customerParser = new CustomerParser();
  private final AccountParser accountParser = new AccountParser();
  private final TransactionParser transactionParser = new TransactionParser();
  private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);

  public GenerationReport generate(Path dataDirectory, Path outputDirectory) throws IOException {
    Map<String, Customer> customers = read(dataDirectory.resolve("custdata.txt")).stream()
        .collect(Collectors.toMap(r -> r.substring(0, 9), customerParser::parse, (a, b) -> a));
    Map<String, Account> accounts = read(dataDirectory.resolve("acctdata.txt")).stream()
        .collect(Collectors.toMap(r -> r.substring(0, 11), accountParser::parse, (a, b) -> a));
    List<String> anomalies = new ArrayList<>();
    Map<String, List<Transaction>> transactions = new LinkedHashMap<>();
    for (String line : read(dataDirectory.resolve("dailytran.txt"))) {
      try {
        Transaction transaction = transactionParser.parse(line);
        transactions.computeIfAbsent(transaction.cardNumber(), ignored -> new ArrayList<>()).add(transaction);
      } catch (RuntimeException e) {
        anomalies.add("Malformed transaction: " + e.getMessage());
      }
    }
    List<CardXref> xrefs = new ArrayList<>();
    for (String line : read(dataDirectory.resolve("cardxref.txt"))) {
      try { xrefs.add(xrefParser.parse(line)); }
      catch (RuntimeException e) { anomalies.add("Malformed card xref: " + e.getMessage()); }
    }
    Files.createDirectories(outputDirectory);
    Set<String> generated = new LinkedHashSet<>();
    for (CardXref xref : xrefs) {
      Customer customer = customers.get(xref.customerId());
      Account account = accounts.get(xref.accountId());
      if (customer == null) { anomalies.add("Missing customer " + xref.customerId() + " for card " + xref.cardNumber()); continue; }
      if (account == null) { anomalies.add("Missing account " + xref.accountId() + " for card " + xref.cardNumber()); continue; }
      List<Transaction> cardTransactions = transactions.getOrDefault(xref.cardNumber(), List.of());
      Statement statement = Statement.from(account.accountId(), customer, xref.cardNumber(), cardTransactions);
      Path file = outputDirectory.resolve("statement-" + account.accountId() + ".json");
      mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), statement);
      generated.add(account.accountId());
    }
    return new GenerationReport(generated.size(), transactions.values().stream().mapToInt(List::size).sum(), anomalies);
  }

  private static List<String> read(Path path) throws IOException {
    return Files.readAllLines(path).stream().filter(line -> !line.isEmpty()).toList();
  }
  public record GenerationReport(int statements, int transactions, List<String> anomalies) {}
}
