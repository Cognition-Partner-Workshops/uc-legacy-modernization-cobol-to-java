package com.carddemo.statement;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The STEP040 input files of {@code CREASTMT.JCL}, read from {@code app/data/ASCII}.
 *
 * <p>XREFFILE is presented in ascending card-number order and CUSTFILE / ACCTFILE as
 * keyed lookups, mirroring the KSDS access CBSTM03B performs.
 */
public final class CardDemoData {

    private final List<CardXrefRecord> xrefRecords;
    private final Map<String, CustomerRecord> customersById;
    private final Map<String, AccountRecord> accountsById;
    private final List<TransactionRecord> transactions;

    private CardDemoData(List<CardXrefRecord> xrefRecords,
                         Map<String, CustomerRecord> customersById,
                         Map<String, AccountRecord> accountsById,
                         List<TransactionRecord> transactions) {
        this.xrefRecords = xrefRecords;
        this.customersById = customersById;
        this.accountsById = accountsById;
        this.transactions = transactions;
    }

    public static CardDemoData load(Path asciiDir) {
        List<CardXrefRecord> xrefs = new ArrayList<>();
        for (String line : readRecords(asciiDir.resolve("cardxref.txt"), CardXrefRecord.RECORD_LENGTH)) {
            xrefs.add(CardXrefRecord.parse(line));
        }
        xrefs.sort(Comparator.comparing(CardXrefRecord::cardNumber));

        Map<String, CustomerRecord> customers = new LinkedHashMap<>();
        for (String line : readRecords(asciiDir.resolve("custdata.txt"), CustomerRecord.RECORD_LENGTH)) {
            CustomerRecord customer = CustomerRecord.parse(line);
            customers.put(customer.customerId(), customer);
        }

        Map<String, AccountRecord> accounts = new LinkedHashMap<>();
        for (String line : readRecords(asciiDir.resolve("acctdata.txt"), AccountRecord.RECORD_LENGTH)) {
            AccountRecord account = AccountRecord.parse(line);
            accounts.put(account.accountId(), account);
        }

        List<TransactionRecord> transactions = JclTransactionSort.reshapeAndSort(
                readRecords(asciiDir.resolve("dailytran.txt"), TransactionRecord.RECORD_LENGTH));

        return new CardDemoData(xrefs, customers, accounts, transactions);
    }

    /** Reads one fixed-width record per line, padding stripped trailing filler. */
    static List<String> readRecords(Path file, int recordLength) {
        List<String> records = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.ISO_8859_1)) {
                if (line.isBlank()) {
                    continue;
                }
                records.add(CobolText.alphanumeric(line, recordLength));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
        return records;
    }

    public List<CardXrefRecord> xrefRecords() {
        return xrefRecords;
    }

    public List<TransactionRecord> transactions() {
        return transactions;
    }

    /** CUSTFILE keyed read (CBSTM03B {@code 3000-CUSTFILE-PROC}). */
    public CustomerRecord customer(String customerId) {
        CustomerRecord customer = customersById.get(customerId);
        if (customer == null) {
            throw new IllegalStateException("ERROR READING CUSTFILE for key " + customerId);
        }
        return customer;
    }

    /** ACCTFILE keyed read (CBSTM03B {@code 4000-ACCTFILE-PROC}). */
    public AccountRecord account(String accountId) {
        AccountRecord account = accountsById.get(accountId);
        if (account == null) {
            throw new IllegalStateException("ERROR READING ACCTFILE for key " + accountId);
        }
        return account;
    }
}
