package com.aws.carddemo.batch;

import com.aws.carddemo.domain.Account;
import com.aws.carddemo.domain.Card;
import com.aws.carddemo.domain.CardXref;
import com.aws.carddemo.domain.Customer;
import com.aws.carddemo.domain.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

final class ExportCodec {
  static final int RECORD_LENGTH = 500;
  private static final DateTimeFormatter EXPORT_TIMESTAMP =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss'.00'");

  private ExportCodec() {}

  static String customer(int sequence, Customer customer) {
    return record(
        'C',
        sequence,
        List.of(
            integer(customer.getCustId()),
            text(customer.getFirstName()),
            text(customer.getMiddleName()),
            text(customer.getLastName()),
            text(customer.getAddrLine1()),
            text(customer.getAddrLine2()),
            text(customer.getAddrLine3()),
            text(customer.getAddrStateCd()),
            text(customer.getAddrCountryCd()),
            text(customer.getAddrZip()),
            text(customer.getPhoneNum1()),
            text(customer.getPhoneNum2()),
            integer(customer.getSsn()),
            text(customer.getGovtIssuedId()),
            text(customer.getDobYyyyMmDd()),
            text(customer.getEftAccountId()),
            text(customer.getPriCardHolderInd()),
            integer(customer.getFicoCreditScore())));
  }

  static String account(int sequence, Account account) {
    return record(
        'A',
        sequence,
        List.of(
            longValue(account.getAcctId()),
            text(account.getActiveStatus()),
            decimal(account.getCurrBal()),
            decimal(account.getCreditLimit()),
            decimal(account.getCashCreditLimit()),
            text(account.getOpenDate()),
            text(account.getExpiraionDate()),
            text(account.getReissueDate()),
            decimal(account.getCurrCycCredit()),
            decimal(account.getCurrCycDebit()),
            text(account.getAddrZip()),
            text(account.getGroupId())));
  }

  static String xref(int sequence, CardXref xref) {
    return record(
        'X',
        sequence,
        List.of(
            text(xref.getId().getCardNum()),
            integer(xref.getCustId()),
            longValue(xref.getAcctId())));
  }

  static String transaction(int sequence, Transaction transaction) {
    return record(
        'T',
        sequence,
        List.of(
            text(transaction.getTranId()),
            text(transaction.getTypeCd()),
            integer(transaction.getCatCd()),
            text(transaction.getSource()),
            text(transaction.getTranDesc()),
            decimal(transaction.getAmt()),
            integer(transaction.getMerchantId()),
            text(transaction.getMerchantName()),
            text(transaction.getMerchantCity()),
            text(transaction.getMerchantZip()),
            text(transaction.getCardNum()),
            text(transaction.getOrigTs()),
            text(transaction.getProcTs())));
  }

  static String card(int sequence, Card card) {
    return record(
        'D',
        sequence,
        List.of(
            text(card.getCardNum()),
            longValue(card.getAcctId()),
            integer(card.getCvvCd()),
            text(card.getEmbossedName()),
            text(card.getExpiraionDate()),
            text(card.getActiveStatus())));
  }

  static ExportRecord decode(String line) {
    List<String> fields = split(line.trim());
    if (fields.size() < 6) {
      throw new IllegalArgumentException("Export record is missing common header fields");
    }
    return new ExportRecord(
        fields.get(0).charAt(0), Integer.parseInt(fields.get(2)), fields.subList(5, fields.size()));
  }

  static String timestamp() {
    return LocalDateTime.now().format(EXPORT_TIMESTAMP);
  }

  static String integer(Integer value) {
    return value == null ? "" : value.toString();
  }

  static String longValue(Long value) {
    return value == null ? "" : value.toString();
  }

  static String decimal(BigDecimal value) {
    return value == null ? "" : value.toPlainString();
  }

  static String text(String value) {
    return value == null ? "" : value;
  }

  private static String record(char type, int sequence, List<String> fields) {
    String payload =
        fields.stream().map(ExportCodec::escape).collect(java.util.stream.Collectors.joining("|"));
    String value =
        type + "|" + timestamp() + "|" + String.format("%09d", sequence) + "|0001|NORTH|" + payload;
    if (value.length() > RECORD_LENGTH) {
      throw new IllegalArgumentException("Export record exceeds 500 characters");
    }
    return String.format("%-" + RECORD_LENGTH + "s", value);
  }

  private static String escape(String value) {
    return value.replace("\\", "\\\\").replace("|", "\\|");
  }

  private static List<String> split(String value) {
    List<String> fields = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean escaped = false;
    for (char c : value.toCharArray()) {
      if (escaped) {
        current.append(c);
        escaped = false;
      } else if (c == '\\') {
        escaped = true;
      } else if (c == '|') {
        fields.add(current.toString());
        current.setLength(0);
      } else {
        current.append(c);
      }
    }
    fields.add(current.toString());
    return fields;
  }

  record ExportRecord(char type, int sequence, List<String> fields) {}
}
