package com.carddemo.batch.job;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.CardXrefRecord;
import com.carddemo.batch.model.CustomerRecord;
import com.carddemo.batch.model.TransactionRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parity tests for StatementGenerationJob (Java equivalent of CBSTM03A.CBL).
 * Verifies statement generation in both plain text and HTML formats.
 */
class StatementGenerationJobTest {

    private List<CardXrefRecord> xrefRecords;
    private Map<String, CardXrefRecord> xrefByCardNum;
    private Map<Long, CustomerRecord> customersByCustomerId;
    private Map<Long, AccountRecord> accountsByAcctId;
    private Map<String, List<TransactionRecord>> transactionsByCardNum;

    @BeforeEach
    void setUp() {
        xrefRecords = new ArrayList<>();
        xrefByCardNum = new HashMap<>();
        customersByCustomerId = new HashMap<>();
        accountsByAcctId = new HashMap<>();
        transactionsByCardNum = new HashMap<>();

        // Set up test data
        CardXrefRecord xref = new CardXrefRecord("4111111111111111", 100001, 12345678901L);
        xrefRecords.add(xref);
        xrefByCardNum.put(xref.getCardNum(), xref);

        CustomerRecord customer = new CustomerRecord();
        customer.setCustId(100001);
        customer.setFirstName("John");
        customer.setMiddleName("Q");
        customer.setLastName("Public");
        customer.setAddrLine1("123 Main Street");
        customer.setAddrLine2("Apt 4B");
        customer.setAddrLine3("Anytown");
        customer.setAddrStateCd("CA");
        customer.setAddrZip("90210");
        customer.setAddrCountryCd("US");
        customer.setFicoCreditScore(750);
        customersByCustomerId.put(customer.getCustId(), customer);

        AccountRecord account = new AccountRecord();
        account.setAcctId(12345678901L);
        account.setActiveStatus("Y");
        account.setCurrBal(new BigDecimal("2500.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));
        accountsByAcctId.put(account.getAcctId(), account);

        // Set up transactions for the card
        List<TransactionRecord> transactions = new ArrayList<>();
        TransactionRecord tran1 = new TransactionRecord();
        tran1.setTranId("TRAN000000000001");
        tran1.setDescription("Grocery Store Purchase");
        tran1.setAmount(new BigDecimal("45.99"));
        transactions.add(tran1);

        TransactionRecord tran2 = new TransactionRecord();
        tran2.setTranId("TRAN000000000002");
        tran2.setDescription("Gas Station");
        tran2.setAmount(new BigDecimal("38.50"));
        transactions.add(tran2);

        transactionsByCardNum.put("4111111111111111", transactions);
    }

    @Test
    void testStatementGenerationProducesOutput() {
        StatementGenerationJob job = new StatementGenerationJob(
                xrefRecords, xrefByCardNum, customersByCustomerId,
                accountsByAcctId, transactionsByCardNum);

        int rc = job.execute();

        assertEquals(0, rc);
        assertFalse(job.getStatementLines().isEmpty(), "Should produce statement lines");
        assertFalse(job.getHtmlLines().isEmpty(), "Should produce HTML lines");
    }

    @Test
    void testStatementContainsCustomerName() {
        StatementGenerationJob job = new StatementGenerationJob(
                xrefRecords, xrefByCardNum, customersByCustomerId,
                accountsByAcctId, transactionsByCardNum);

        job.execute();

        boolean hasName = job.getStatementLines().stream()
                .anyMatch(line -> line.contains("John") && line.contains("Public"));
        assertTrue(hasName, "Statement should contain customer name");
    }

    @Test
    void testStatementContainsAccountId() {
        StatementGenerationJob job = new StatementGenerationJob(
                xrefRecords, xrefByCardNum, customersByCustomerId,
                accountsByAcctId, transactionsByCardNum);

        job.execute();

        boolean hasAcctId = job.getStatementLines().stream()
                .anyMatch(line -> line.contains("12345678901"));
        assertTrue(hasAcctId, "Statement should contain account ID");
    }

    @Test
    void testStatementContainsTransactions() {
        StatementGenerationJob job = new StatementGenerationJob(
                xrefRecords, xrefByCardNum, customersByCustomerId,
                accountsByAcctId, transactionsByCardNum);

        job.execute();

        boolean hasTran1 = job.getStatementLines().stream()
                .anyMatch(line -> line.contains("Grocery Store Purchase"));
        boolean hasTran2 = job.getStatementLines().stream()
                .anyMatch(line -> line.contains("Gas Station"));
        assertTrue(hasTran1, "Statement should contain first transaction");
        assertTrue(hasTran2, "Statement should contain second transaction");
    }

    @Test
    void testStatementHasStartAndEndMarkers() {
        StatementGenerationJob job = new StatementGenerationJob(
                xrefRecords, xrefByCardNum, customersByCustomerId,
                accountsByAcctId, transactionsByCardNum);

        job.execute();

        List<String> lines = job.getStatementLines();
        assertTrue(lines.get(0).contains("START OF STATEMENT"),
                "First line should contain START OF STATEMENT");
        assertTrue(lines.get(lines.size() - 1).contains("END OF STATEMENT"),
                "Last line should contain END OF STATEMENT");
    }

    @Test
    void testHtmlContainsAccountNumber() {
        StatementGenerationJob job = new StatementGenerationJob(
                xrefRecords, xrefByCardNum, customersByCustomerId,
                accountsByAcctId, transactionsByCardNum);

        job.execute();

        boolean hasAcctId = job.getHtmlLines().stream()
                .anyMatch(line -> line.contains("12345678901"));
        assertTrue(hasAcctId, "HTML should contain account number");
    }

    @Test
    void testHtmlContainsEndOfStatement() {
        StatementGenerationJob job = new StatementGenerationJob(
                xrefRecords, xrefByCardNum, customersByCustomerId,
                accountsByAcctId, transactionsByCardNum);

        job.execute();

        boolean hasEnd = job.getHtmlLines().stream()
                .anyMatch(line -> line.contains("End of Statement"));
        assertTrue(hasEnd, "HTML should contain 'End of Statement'");
    }

    @Test
    void testStatementWithNoTransactions() {
        transactionsByCardNum.clear();

        StatementGenerationJob job = new StatementGenerationJob(
                xrefRecords, xrefByCardNum, customersByCustomerId,
                accountsByAcctId, transactionsByCardNum);

        int rc = job.execute();

        assertEquals(0, rc);
        // Should still produce a statement with headers but no transactions
        assertFalse(job.getStatementLines().isEmpty());
    }

    @Test
    void testMissingCustomerSkipsStatement() {
        customersByCustomerId.clear();

        StatementGenerationJob job = new StatementGenerationJob(
                xrefRecords, xrefByCardNum, customersByCustomerId,
                accountsByAcctId, transactionsByCardNum);

        int rc = job.execute();

        assertEquals(0, rc);
        assertTrue(job.getStatementLines().isEmpty(),
                "No statements should be generated when customer is missing");
    }

    @Test
    void testStatementTotalAmount() {
        StatementGenerationJob job = new StatementGenerationJob(
                xrefRecords, xrefByCardNum, customersByCustomerId,
                accountsByAcctId, transactionsByCardNum);

        job.execute();

        // Total = 45.99 + 38.50 = 84.49
        boolean hasTotal = job.getStatementLines().stream()
                .anyMatch(line -> line.contains("Total EXP") && line.contains("84.49"));
        assertTrue(hasTotal, "Statement should contain correct total amount");
    }
}
