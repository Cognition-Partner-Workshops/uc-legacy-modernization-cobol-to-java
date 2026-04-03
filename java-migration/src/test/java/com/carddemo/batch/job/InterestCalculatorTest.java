package com.carddemo.batch.job;

import com.carddemo.batch.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InterestCalculatorTest {

    @TempDir
    Path tempDir;

    private Map<Long, AccountRecord> accountMap;
    private Map<Long, CardXrefRecord> xrefByAcctId;
    private Map<String, DisclosureGroupRecord> disclosureGroupMap;
    private Path transactionOutputFile;

    @BeforeEach
    void setUp() {
        accountMap = new HashMap<>();
        xrefByAcctId = new HashMap<>();
        disclosureGroupMap = new HashMap<>();
        transactionOutputFile = tempDir.resolve("tranout.txt");

        // Set up account
        AccountRecord acct = new AccountRecord();
        acct.setAcctId(1L);
        acct.setAcctActiveStatus("Y");
        acct.setAcctCurrBal(new BigDecimal("10000.00"));
        acct.setAcctCreditLimit(new BigDecimal("50000.00"));
        acct.setAcctCashCreditLimit(new BigDecimal("10000.00"));
        acct.setAcctGroupId("GRP001");
        acct.setAcctCurrCycCredit(new BigDecimal("500.00"));
        acct.setAcctCurrCycDebit(new BigDecimal("300.00"));
        accountMap.put(1L, acct);

        // Set up xref
        CardXrefRecord xref = new CardXrefRecord();
        xref.setXrefCardNum("4111111111111111");
        xref.setXrefCustId(1L);
        xref.setXrefAcctId(1L);
        xrefByAcctId.put(1L, xref);

        // Set up disclosure group with 12% annual rate
        DisclosureGroupRecord disc = new DisclosureGroupRecord();
        disc.setDisAcctGroupId("GRP001");
        disc.setDisTranTypeCd("01");
        disc.setDisTranCatCd(5);
        disc.setDisIntRate(new BigDecimal("12.00"));
        String key = String.format("%-10s%-2s%04d", "GRP001", "01", 5);
        disclosureGroupMap.put(key, disc);
    }

    @Test
    void executeCalculatesInterestAndWritesTransactions() throws IOException {
        List<TranCatBalRecord> catBalRecords = new ArrayList<>();
        TranCatBalRecord catBal = new TranCatBalRecord();
        catBal.setTrancatAcctId(1L);
        catBal.setTrancatTypeCd("01");
        catBal.setTrancatCd(5);
        catBal.setTranCatBal(new BigDecimal("5000.00"));
        catBalRecords.add(catBal);

        InterestCalculator calculator = new InterestCalculator(
                catBalRecords, accountMap, xrefByAcctId, disclosureGroupMap,
                transactionOutputFile, "20250101");

        List<TransactionRecord> transactions = calculator.execute();

        assertEquals(1, transactions.size());
        TransactionRecord txn = transactions.get(0);
        // Interest = (5000 * 12) / 1200 = 50.00
        assertEquals(new BigDecimal("50.00"), txn.getTranAmt());
        assertEquals("01", txn.getTranTypeCd());
        assertEquals(5, txn.getTranCatCd());
        assertTrue(txn.getTranDesc().contains("Int. for a/c 1"));

        assertTrue(Files.exists(transactionOutputFile));
        List<String> lines = Files.readAllLines(transactionOutputFile);
        assertEquals(1, lines.size());

        // Check account was updated
        AccountRecord updatedAcct = accountMap.get(1L);
        assertEquals(new BigDecimal("10050.00"), updatedAcct.getAcctCurrBal());
        assertEquals(BigDecimal.ZERO, updatedAcct.getAcctCurrCycCredit());
        assertEquals(BigDecimal.ZERO, updatedAcct.getAcctCurrCycDebit());
    }

    @Test
    void computeInterestCalculatesCorrectly() throws IOException {
        InterestCalculator calculator = new InterestCalculator(
                List.of(), accountMap, xrefByAcctId, disclosureGroupMap,
                transactionOutputFile, "20250101");

        // (10000 * 18) / 1200 = 150.00
        BigDecimal result = calculator.computeInterest(
                new BigDecimal("10000.00"), new BigDecimal("18.00"));
        assertEquals(new BigDecimal("150.00"), result);

        // (0 * 12) / 1200 = 0.00
        BigDecimal zeroResult = calculator.computeInterest(
                BigDecimal.ZERO, new BigDecimal("12.00"));
        assertEquals(new BigDecimal("0.00"), zeroResult);
    }

    @Test
    void getInterestRateFallsBackToDefaultGroup() throws IOException {
        // Add a DEFAULT group record
        DisclosureGroupRecord defaultDisc = new DisclosureGroupRecord();
        defaultDisc.setDisAcctGroupId("DEFAULT");
        defaultDisc.setDisTranTypeCd("01");
        defaultDisc.setDisTranCatCd(5);
        defaultDisc.setDisIntRate(new BigDecimal("10.00"));
        String defaultKey = String.format("%-10s%-2s%04d", "DEFAULT", "01", 5);
        disclosureGroupMap.put(defaultKey, defaultDisc);

        InterestCalculator calculator = new InterestCalculator(
                List.of(), accountMap, xrefByAcctId, disclosureGroupMap,
                transactionOutputFile, "20250101");

        // Look up with non-existent group - should fall back to DEFAULT
        DisclosureGroupRecord result = calculator.getInterestRate("UNKNOWN", "01", 5);
        assertNotNull(result);
        assertEquals(new BigDecimal("10.00"), result.getDisIntRate());
    }

    @Test
    void executeHandlesEmptyCatBalList() throws IOException {
        InterestCalculator calculator = new InterestCalculator(
                List.of(), accountMap, xrefByAcctId, disclosureGroupMap,
                transactionOutputFile, "20250101");

        List<TransactionRecord> transactions = calculator.execute();
        assertTrue(transactions.isEmpty());
        assertEquals(0, calculator.getRecordCount());
    }

    @Test
    void executeHandlesMissingAccount() throws IOException {
        List<TranCatBalRecord> catBalRecords = new ArrayList<>();
        TranCatBalRecord catBal = new TranCatBalRecord();
        catBal.setTrancatAcctId(999L); // non-existent account
        catBal.setTrancatTypeCd("01");
        catBal.setTrancatCd(5);
        catBal.setTranCatBal(new BigDecimal("5000.00"));
        catBalRecords.add(catBal);

        InterestCalculator calculator = new InterestCalculator(
                catBalRecords, accountMap, xrefByAcctId, disclosureGroupMap,
                transactionOutputFile, "20250101");

        List<TransactionRecord> transactions = calculator.execute();
        assertTrue(transactions.isEmpty());
    }

    @Test
    void updateAccountAddsInterestToBalance() throws IOException {
        InterestCalculator calculator = new InterestCalculator(
                List.of(), accountMap, xrefByAcctId, disclosureGroupMap,
                transactionOutputFile, "20250101");

        AccountRecord acct = accountMap.get(1L);
        calculator.updateAccount(acct, new BigDecimal("75.50"));

        assertEquals(new BigDecimal("10075.50"), acct.getAcctCurrBal());
        assertEquals(BigDecimal.ZERO, acct.getAcctCurrCycCredit());
        assertEquals(BigDecimal.ZERO, acct.getAcctCurrCycDebit());
    }
}
