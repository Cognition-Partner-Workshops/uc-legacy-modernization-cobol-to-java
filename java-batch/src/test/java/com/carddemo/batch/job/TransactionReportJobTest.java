package com.carddemo.batch.job;

import com.carddemo.batch.model.CardXrefRecord;
import com.carddemo.batch.model.TransactionCategoryRecord;
import com.carddemo.batch.model.TransactionRecord;
import com.carddemo.batch.model.TransactionTypeRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parity tests for TransactionReportJob (Java equivalent of CBTRN03C.CBL).
 * Verifies report generation with headers, detail lines, page/account/grand totals.
 */
class TransactionReportJobTest {

    private List<TransactionRecord> transactions;
    private Map<String, CardXrefRecord> xrefByCardNum;
    private Map<String, TransactionTypeRecord> tranTypeByCode;
    private Map<String, TransactionCategoryRecord> tranCatByKey;

    @BeforeEach
    void setUp() {
        transactions = new ArrayList<>();
        xrefByCardNum = new HashMap<>();
        tranTypeByCode = new HashMap<>();
        tranCatByKey = new HashMap<>();

        // Set up cross-reference
        CardXrefRecord xref = new CardXrefRecord("4111111111111111", 100001, 12345678901L);
        xrefByCardNum.put(xref.getCardNum(), xref);

        // Set up transaction type lookup
        tranTypeByCode.put("01", new TransactionTypeRecord("01", "Purchase"));
        tranTypeByCode.put("02", new TransactionTypeRecord("02", "Return"));

        // Set up transaction category lookup
        tranCatByKey.put(String.format("%2s%04d", "01", 1),
                new TransactionCategoryRecord("01", 1, "Retail"));
        tranCatByKey.put(String.format("%2s%04d", "01", 2),
                new TransactionCategoryRecord("01", 2, "Online"));
    }

    @Test
    void testReportGenerationWithTransactions() {
        transactions.add(createTransaction("T001", "01", 1, "POS",
                new BigDecimal("100.00"), "4111111111111111", "2026-01-15-10.30.00.000000"));
        transactions.add(createTransaction("T002", "01", 2, "Online",
                new BigDecimal("75.50"), "4111111111111111", "2026-01-16-14.00.00.000000"));

        TransactionReportJob job = new TransactionReportJob(
                transactions, xrefByCardNum, tranTypeByCode, tranCatByKey);

        int rc = job.execute("2026-01-01", "2026-01-31");

        assertEquals(0, rc);
        assertFalse(job.getReportLines().isEmpty(), "Should produce report lines");
    }

    @Test
    void testReportContainsHeaders() {
        transactions.add(createTransaction("T001", "01", 1, "POS",
                new BigDecimal("100.00"), "4111111111111111", "2026-01-15-10.30.00.000000"));

        TransactionReportJob job = new TransactionReportJob(
                transactions, xrefByCardNum, tranTypeByCode, tranCatByKey);

        job.execute("2026-01-01", "2026-01-31");

        boolean hasReportName = job.getReportLines().stream()
                .anyMatch(line -> line.contains("DALYREPT") && line.contains("Daily Transaction Report"));
        assertTrue(hasReportName, "Report should contain report name header");

        boolean hasColumnHeaders = job.getReportLines().stream()
                .anyMatch(line -> line.contains("Transaction ID") && line.contains("Account ID"));
        assertTrue(hasColumnHeaders, "Report should contain column headers");
    }

    @Test
    void testReportContainsTransactionDetails() {
        transactions.add(createTransaction("T001", "01", 1, "POS",
                new BigDecimal("100.00"), "4111111111111111", "2026-01-15-10.30.00.000000"));

        TransactionReportJob job = new TransactionReportJob(
                transactions, xrefByCardNum, tranTypeByCode, tranCatByKey);

        job.execute("2026-01-01", "2026-01-31");

        boolean hasTranId = job.getReportLines().stream()
                .anyMatch(line -> line.contains("T001"));
        assertTrue(hasTranId, "Report should contain transaction ID");

        boolean hasAmount = job.getReportLines().stream()
                .anyMatch(line -> line.contains("100.00"));
        assertTrue(hasAmount, "Report should contain transaction amount");
    }

    @Test
    void testDateRangeFiltering() {
        // This transaction is within range
        transactions.add(createTransaction("T001", "01", 1, "POS",
                new BigDecimal("100.00"), "4111111111111111", "2026-01-15-10.30.00.000000"));
        // This transaction is outside range
        transactions.add(createTransaction("T002", "01", 1, "POS",
                new BigDecimal("200.00"), "4111111111111111", "2026-03-15-10.30.00.000000"));

        TransactionReportJob job = new TransactionReportJob(
                transactions, xrefByCardNum, tranTypeByCode, tranCatByKey);

        job.execute("2026-01-01", "2026-01-31");

        boolean hasT001 = job.getReportLines().stream()
                .anyMatch(line -> line.contains("T001"));
        boolean hasT002 = job.getReportLines().stream()
                .anyMatch(line -> line.contains("T002"));

        assertTrue(hasT001, "T001 should be in report (within date range)");
        assertFalse(hasT002, "T002 should NOT be in report (outside date range)");
    }

    @Test
    void testGrandTotalIsPresent() {
        transactions.add(createTransaction("T001", "01", 1, "POS",
                new BigDecimal("100.00"), "4111111111111111", "2026-01-15-10.30.00.000000"));
        transactions.add(createTransaction("T002", "01", 2, "Online",
                new BigDecimal("50.00"), "4111111111111111", "2026-01-16-14.00.00.000000"));

        TransactionReportJob job = new TransactionReportJob(
                transactions, xrefByCardNum, tranTypeByCode, tranCatByKey);

        job.execute("2026-01-01", "2026-01-31");

        boolean hasGrandTotal = job.getReportLines().stream()
                .anyMatch(line -> line.contains("Grand Total"));
        assertTrue(hasGrandTotal, "Report should contain Grand Total");
    }

    @Test
    void testEmptyReportWhenNoTransactionsInRange() {
        transactions.add(createTransaction("T001", "01", 1, "POS",
                new BigDecimal("100.00"), "4111111111111111", "2026-06-15-10.30.00.000000"));

        TransactionReportJob job = new TransactionReportJob(
                transactions, xrefByCardNum, tranTypeByCode, tranCatByKey);

        job.execute("2026-01-01", "2026-01-31");

        // No detail lines should be generated
        boolean hasGrandTotal = job.getReportLines().stream()
                .anyMatch(line -> line.contains("Grand Total"));
        assertFalse(hasGrandTotal, "Report should not have Grand Total when no transactions match");
    }

    @Test
    void testTransactionTypeLookup() {
        transactions.add(createTransaction("T001", "01", 1, "POS",
                new BigDecimal("100.00"), "4111111111111111", "2026-01-15-10.30.00.000000"));

        TransactionReportJob job = new TransactionReportJob(
                transactions, xrefByCardNum, tranTypeByCode, tranCatByKey);

        job.execute("2026-01-01", "2026-01-31");

        boolean hasTypeDesc = job.getReportLines().stream()
                .anyMatch(line -> line.contains("Purchase"));
        assertTrue(hasTypeDesc, "Report should contain transaction type description");
    }

    @Test
    void testTransactionCategoryLookup() {
        transactions.add(createTransaction("T001", "01", 1, "POS",
                new BigDecimal("100.00"), "4111111111111111", "2026-01-15-10.30.00.000000"));

        TransactionReportJob job = new TransactionReportJob(
                transactions, xrefByCardNum, tranTypeByCode, tranCatByKey);

        job.execute("2026-01-01", "2026-01-31");

        boolean hasCatDesc = job.getReportLines().stream()
                .anyMatch(line -> line.contains("Retail"));
        assertTrue(hasCatDesc, "Report should contain transaction category description");
    }

    private TransactionRecord createTransaction(String tranId, String typeCd, int catCd,
                                                 String source, BigDecimal amount,
                                                 String cardNum, String procTs) {
        TransactionRecord tran = new TransactionRecord();
        tran.setTranId(tranId);
        tran.setTypeCd(typeCd);
        tran.setCatCd(catCd);
        tran.setSource(source);
        tran.setAmount(amount);
        tran.setCardNum(cardNum);
        tran.setProcTimestamp(procTs);
        return tran;
    }
}
