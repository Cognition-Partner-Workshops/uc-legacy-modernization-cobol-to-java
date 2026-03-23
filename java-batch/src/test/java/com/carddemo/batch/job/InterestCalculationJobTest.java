package com.carddemo.batch.job;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.CardXrefRecord;
import com.carddemo.batch.model.DisclosureGroupRecord;
import com.carddemo.batch.model.TranCatBalRecord;
import com.carddemo.batch.model.TransactionRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parity tests for InterestCalculationJob (Java equivalent of CBACT04C.CBL).
 * Verifies that monthly interest is computed correctly using disclosure group
 * rates and that account balances are updated accordingly.
 */
class InterestCalculationJobTest {

    private List<TranCatBalRecord> tranCatBalRecords;
    private Map<Long, CardXrefRecord> xrefByAcctId;
    private Map<Long, AccountRecord> accountsByAcctId;
    private Map<String, DisclosureGroupRecord> disclosureGroupByKey;

    @BeforeEach
    void setUp() {
        tranCatBalRecords = new ArrayList<>();
        xrefByAcctId = new HashMap<>();
        accountsByAcctId = new HashMap<>();
        disclosureGroupByKey = new HashMap<>();

        // Set up account
        AccountRecord acct = new AccountRecord();
        acct.setAcctId(12345678901L);
        acct.setActiveStatus("Y");
        acct.setCurrBal(new BigDecimal("1000.00"));
        acct.setGroupId("GRP01");
        accountsByAcctId.put(acct.getAcctId(), acct);

        // Set up cross-reference
        CardXrefRecord xref = new CardXrefRecord("4111111111111111", 100001, 12345678901L);
        xrefByAcctId.put(12345678901L, xref);

        // Set up disclosure group with 12% annual rate
        DisclosureGroupRecord discGroup = new DisclosureGroupRecord("GRP01", "01", 1,
                new BigDecimal("12.00"));
        disclosureGroupByKey.put(discGroup.getKey(), discGroup);

        // Set up default disclosure group
        DisclosureGroupRecord defaultGroup = new DisclosureGroupRecord("DEFAULT", "01", 1,
                new BigDecimal("18.00"));
        disclosureGroupByKey.put(defaultGroup.getKey(), defaultGroup);
    }

    @Test
    void testInterestCalculation() {
        // TranCatBal with $1000 balance, type 01, cat 1
        TranCatBalRecord catBal = new TranCatBalRecord(12345678901L, "01", 1,
                new BigDecimal("1000.00"));
        tranCatBalRecords.add(catBal);

        InterestCalculationJob job = new InterestCalculationJob(
                tranCatBalRecords, xrefByAcctId, accountsByAcctId, disclosureGroupByKey);

        int rc = job.execute("2026-01-15");

        assertEquals(0, rc);
        // Monthly interest = 1000 * 12 / 1200 = 10.00
        List<TransactionRecord> interestTxns = job.getInterestTransactions();
        assertEquals(1, interestTxns.size());
        assertEquals(new BigDecimal("10.00"), interestTxns.get(0).getAmount());
    }

    @Test
    void testInterestCalculationWithFractionalRate() {
        TranCatBalRecord catBal = new TranCatBalRecord(12345678901L, "01", 1,
                new BigDecimal("2500.00"));
        tranCatBalRecords.add(catBal);

        // Change rate to 15.5%
        DisclosureGroupRecord discGroup = new DisclosureGroupRecord("GRP01", "01", 1,
                new BigDecimal("15.50"));
        disclosureGroupByKey.put(discGroup.getKey(), discGroup);

        InterestCalculationJob job = new InterestCalculationJob(
                tranCatBalRecords, xrefByAcctId, accountsByAcctId, disclosureGroupByKey);

        int rc = job.execute("2026-01-15");

        assertEquals(0, rc);
        // Monthly interest = 2500 * 15.50 / 1200 = 32.29 (HALF_UP)
        BigDecimal expected = new BigDecimal("2500.00")
                .multiply(new BigDecimal("15.50"))
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);
        assertEquals(expected, job.getInterestTransactions().get(0).getAmount());
    }

    @Test
    void testFallbackToDefaultRate() {
        // Account with unknown group
        AccountRecord acct2 = new AccountRecord();
        acct2.setAcctId(99999999999L);
        acct2.setActiveStatus("Y");
        acct2.setCurrBal(new BigDecimal("500.00"));
        acct2.setGroupId("UNKNOWN");
        accountsByAcctId.put(acct2.getAcctId(), acct2);

        xrefByAcctId.put(99999999999L,
                new CardXrefRecord("4333333333333333", 100003, 99999999999L));

        TranCatBalRecord catBal = new TranCatBalRecord(99999999999L, "01", 1,
                new BigDecimal("600.00"));
        tranCatBalRecords.add(catBal);

        InterestCalculationJob job = new InterestCalculationJob(
                tranCatBalRecords, xrefByAcctId, accountsByAcctId, disclosureGroupByKey);

        int rc = job.execute("2026-01-15");

        assertEquals(0, rc);
        // Should use DEFAULT rate of 18%
        // Monthly interest = 600 * 18 / 1200 = 9.00
        assertEquals(new BigDecimal("9.00"), job.getInterestTransactions().get(0).getAmount());
    }

    @Test
    void testAccountBalanceUpdatedWithInterest() {
        TranCatBalRecord catBal = new TranCatBalRecord(12345678901L, "01", 1,
                new BigDecimal("1000.00"));
        tranCatBalRecords.add(catBal);

        BigDecimal originalBal = accountsByAcctId.get(12345678901L).getCurrBal();

        InterestCalculationJob job = new InterestCalculationJob(
                tranCatBalRecords, xrefByAcctId, accountsByAcctId, disclosureGroupByKey);

        job.execute("2026-01-15");

        // Account balance should increase by interest amount (10.00)
        AccountRecord updated = accountsByAcctId.get(12345678901L);
        assertEquals(originalBal.add(new BigDecimal("10.00")), updated.getCurrBal());
    }

    @Test
    void testMultipleCategoriesForSameAccount() {
        tranCatBalRecords.add(new TranCatBalRecord(12345678901L, "01", 1,
                new BigDecimal("1000.00")));
        tranCatBalRecords.add(new TranCatBalRecord(12345678901L, "01", 2,
                new BigDecimal("500.00")));

        // Add disclosure for cat 2
        DisclosureGroupRecord disc2 = new DisclosureGroupRecord("GRP01", "01", 2,
                new BigDecimal("12.00"));
        disclosureGroupByKey.put(disc2.getKey(), disc2);

        InterestCalculationJob job = new InterestCalculationJob(
                tranCatBalRecords, xrefByAcctId, accountsByAcctId, disclosureGroupByKey);

        int rc = job.execute("2026-01-15");

        assertEquals(0, rc);
        assertEquals(2, job.getInterestTransactions().size());

        // Interest 1: 1000 * 12 / 1200 = 10.00
        // Interest 2: 500 * 12 / 1200 = 5.00
        BigDecimal totalInterest = job.getInterestTransactions().stream()
                .map(TransactionRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("15.00"), totalInterest);
    }

    @Test
    void testZeroBalanceProducesZeroInterest() {
        tranCatBalRecords.add(new TranCatBalRecord(12345678901L, "01", 1,
                BigDecimal.ZERO));

        InterestCalculationJob job = new InterestCalculationJob(
                tranCatBalRecords, xrefByAcctId, accountsByAcctId, disclosureGroupByKey);

        int rc = job.execute("2026-01-15");

        assertEquals(0, rc);
        assertEquals(1, job.getInterestTransactions().size());
        assertEquals(new BigDecimal("0.00"), job.getInterestTransactions().get(0).getAmount());
    }

    @Test
    void testInterestTransactionHasDb2Timestamp() {
        tranCatBalRecords.add(new TranCatBalRecord(12345678901L, "01", 1,
                new BigDecimal("1000.00")));

        InterestCalculationJob job = new InterestCalculationJob(
                tranCatBalRecords, xrefByAcctId, accountsByAcctId, disclosureGroupByKey);

        job.execute("2026-01-15");

        TransactionRecord interestTxn = job.getInterestTransactions().get(0);
        assertNotNull(interestTxn.getProcTimestamp());
        // DB2 format: YYYY-MM-DD-HH.MM.SS.HH0000
        assertTrue(interestTxn.getProcTimestamp().matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{8}"),
                "Timestamp should be in DB2 format: " + interestTxn.getProcTimestamp());
    }
}
