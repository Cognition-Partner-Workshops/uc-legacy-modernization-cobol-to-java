package com.aws.carddemo.dataload;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public enum SeedDataset {
  USRSEC("usrsec", 80, Charset.forName("Cp037"), "EBCDIC/AWS.M2.CARDDEMO.USRSEC.PS", null),
  CUSTOMER(
      "customer",
      500,
      StandardCharsets.US_ASCII,
      "ASCII/custdata.txt",
      "EBCDIC/AWS.M2.CARDDEMO.CUSTDATA.PS"),
  ACCOUNT(
      "account",
      300,
      StandardCharsets.US_ASCII,
      "ASCII/acctdata.txt",
      "EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS"),
  CARD(
      "card",
      150,
      StandardCharsets.US_ASCII,
      "ASCII/carddata.txt",
      "EBCDIC/AWS.M2.CARDDEMO.CARDDATA.PS"),
  CARD_XREF(
      "card_xref",
      36,
      StandardCharsets.US_ASCII,
      "ASCII/cardxref.txt",
      "EBCDIC/AWS.M2.CARDDEMO.CARDXREF.PS"),
  DAILY_TRANSACTION(
      "daily_transaction",
      350,
      StandardCharsets.US_ASCII,
      "ASCII/dailytran.txt",
      "EBCDIC/AWS.M2.CARDDEMO.DALYTRAN.PS"),
  DISCLOSURE_GROUP(
      "disclosure_group",
      50,
      StandardCharsets.US_ASCII,
      "ASCII/discgrp.txt",
      "EBCDIC/AWS.M2.CARDDEMO.DISCGRP.PS"),
  TRAN_CATEGORY_BALANCE(
      "tran_category_balance",
      50,
      StandardCharsets.US_ASCII,
      "ASCII/tcatbal.txt",
      "EBCDIC/AWS.M2.CARDDEMO.TCATBALF.PS"),
  TRANSACTION_CATEGORY(
      "transaction_category",
      60,
      StandardCharsets.US_ASCII,
      "ASCII/trancatg.txt",
      "EBCDIC/AWS.M2.CARDDEMO.TRANCATG.PS"),
  TRANSACTION_TYPE(
      "transaction_type",
      60,
      StandardCharsets.US_ASCII,
      "ASCII/trantype.txt",
      "EBCDIC/AWS.M2.CARDDEMO.TRANTYPE.PS");

  private final String tableName;
  private final int recordLength;
  private final Charset asciiCharset;
  private final String asciiRelativePath;
  private final String ebcdicRelativePath;

  SeedDataset(
      String tableName,
      int recordLength,
      Charset asciiCharset,
      String asciiRelativePath,
      String ebcdicRelativePath) {
    this.tableName = tableName;
    this.recordLength = recordLength;
    this.asciiCharset = asciiCharset;
    this.asciiRelativePath = asciiRelativePath;
    this.ebcdicRelativePath = ebcdicRelativePath;
  }

  public String tableName() {
    return tableName;
  }

  public int recordLength() {
    return recordLength;
  }

  public Charset charset() {
    return this == USRSEC ? Charset.forName("Cp037") : asciiCharset;
  }

  public Path resolve(Path root) {
    Path ascii = root.resolve(asciiRelativePath);
    if (java.nio.file.Files.exists(ascii)) {
      return ascii;
    }
    if (ebcdicRelativePath == null) {
      throw new IllegalArgumentException("No seed file exists for " + name() + " under " + root);
    }
    return root.resolve(ebcdicRelativePath);
  }
}
