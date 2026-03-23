package com.carddemo.batch.job;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.CardXrefRecord;
import com.carddemo.batch.model.TranCatBalRecord;
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
 * Parity tests for TransactionPostingJob (Java equivalent of CBTRN02C.CBL).
 * Verifies that the Java implementation produces equivalent results to the
 * original COBOL batch program for transaction validation and posting.
 */
class TransactionPostingJobTest {

    private Map<String, CardXrefRecord> xrefByCardNum;
    private Map<Long, AccountRecord> accountsByAcctId;
    private Map<String, TranCatBalRecord> tranCatBalByKey;

    @BeforeEach
    void setUp() {
        xrefByCardNum = new HashMap<>();
        accountsByAcctId = new HashMap<>();
        tranCatBalByKey = new HashMap<>();

        // Set up test cross-reference data
        CardXrefRecord xref1 = new CardXrefRecord("4111111111111111", 100001, 12345678901L);
        xrefByCardNum.put(xref1.getCardNum(), xref1);

        CardXrefRecord xref2 = new CardXrefRecord("4222222222222222", 100002, 12345678902L);
        xrefByCardNum.put(xref2.getCardNum(), xref2);

        // Set up test account data
        AccountRecord acct1 = new AccountRecord();
        acct1.setAcctId(12345678901L);
        acct1.setActiveStatus("Y");
        acct1.setCurrBal(new BigDecimal("1000.00"));
        acct1.setCreditLimit(new BigDecimal("5000.00"));
        acct1.setCurrCycCredit(new BigDecimal("500.00"));
        acct1.setCurrCycDebit(new BigDecimal("-200.00"));
        acct1.setExpirationDate("2027-12-31");
        accountsByAcctId.put(acct1.getAcctId(), acct1);

        AccountRecord acct2 = new AccountRecord();
        acct2.setAcctId(12345678902L);
        acct2.setActiveStatus("Y");
        acct2.setCurrBal(new BigDecimal("2000.00"));
        acct2.setCreditLimit(new BigDecimal("3000.00"));
        acct2.setCurrCycCredit(new BigDecimal("1000.00"));
        acct2.setCurrCycDebit(new BigDecimal("-500.00"));
        acct2.setExpirationDate("2025-06-30");
        accountsByAcctId.put(acct2.getAcctId(), acct2);
    }

    @Test
    void testValidTransactionIsPosted() {
        TransactionPostingJob job = new TransactionPostingJob(
                xrefByCardNum, accountsByAcctId, tranCatBalByKey);

        TransactionRecord tran = createTransaction(
                "TRAN000000000001", "01", 1, "Online",
                "Test purchase", new BigDecimal("100.00"),
                "4111111111111111", "2026-01-15-10.30.00.000000");

        int rc = job.execute(List.of(tran));

        assertEquals(0, rc, "Return code should be 0 when no rejects");
        assertEquals(1, job.getTransactionCount());
        assertEquals(0, job.getRejectCount());
        assertEquals(1, job.getPostedTransactions().size());
        assertTrue(job.getRejectedTransactions().isEmpty());
    }

    @Test
    void testInvalidCardNumberIsRejected() {
        // COBOL validation reason 100: INVALID CARD NUMBER FOUND
        TransactionPostingJob job = new TransactionPostingJob(
                xrefByCardNum, accountsByAcctId, tranCatBalByKey);

        TransactionRecord tran = createTransaction(
                "TRAN000000000002", "01", 1, "Online",
                "Bad card", new BigDecimal("50.00"),
                "9999999999999999", "2026-01-15-10.30.00.000000");

        int rc = job.execute(List.of(tran));

        assertEquals(4, rc, "Return code should be 4 when rejects exist");
        assertEquals(1, job.getRejectCount());
        assertEquals(100, job.getRejectedTransactions().get(0).failureReason());
    }

    @Test
    void testOverlimitTransactionIsRejected() {
        // COBOL validation reason 102: OVERLIMIT TRANSACTION
        TransactionPostingJob job = new TransactionPostingJob(
                xrefByCardNum, accountsByAcctId, tranCatBalByKey);

        // acct1: creditLimit=5000, currCycCredit=500, currCycDebit=-200
        // tempBal = 500 - (-200) + 4800 = 5500 > 5000 = OVERLIMIT
        TransactionRecord tran = createTransaction(
                "TRAN000000000003", "01", 1, "Online",
                "Big purchase", new BigDecimal("4800.00"),
                "4111111111111111", "2026-01-15-10.30.00.000000");

        int rc = job.execute(List.of(tran));

        assertEquals(4, rc);
        assertEquals(1, job.getRejectCount());
        assertEquals(102, job.getRejectedTransactions().get(0).failureReason());
    }

    @Test
    void testExpiredAccountTransactionIsRejected() {
        // COBOL validation reason 103: TRANSACTION RECEIVED AFTER ACCT EXPIRATION
        TransactionPostingJob job = new TransactionPostingJob(
                xrefByCardNum, accountsByAcctId, tranCatBalByKey);

        // acct2 expiration = 2025-06-30, transaction date = 2026-01-15
        TransactionRecord tran = createTransaction(
                "TRAN000000000004", "01", 1, "Online",
                "After expiry", new BigDecimal("50.00"),
                "4222222222222222", "2026-01-15-10.30.00.000000");

        int rc = job.execute(List.of(tran));

        assertEquals(4, rc);
        assertEquals(1, job.getRejectCount());
        assertEquals(103, job.getRejectedTransactions().get(0).failureReason());
    }

    @Test
    void testAccountBalanceUpdatedAfterPosting() {
        // COBOL 2800-UPDATE-ACCOUNT-REC: ADD DALYTRAN-AMT TO ACCT-CURR-BAL
        TransactionPostingJob job = new TransactionPostingJob(
                xrefByCardNum, accountsByAcctId, tranCatBalByKey);

        BigDecimal originalBal = accountsByAcctId.get(12345678901L).getCurrBal();

        TransactionRecord tran = createTransaction(
                "TRAN000000000005", "01", 1, "Online",
                "Balance test", new BigDecimal("250.00"),
                "4111111111111111", "2026-01-15-10.30.00.000000");

        job.execute(List.of(tran));

        AccountRecord updated = job.getAccountsByAcctId().get(12345678901L);
        assertEquals(originalBal.add(new BigDecimal("250.00")), updated.getCurrBal());
    }

    @Test
    void testCreditCycleUpdatedForPositiveAmount() {
        // COBOL: IF DALYTRAN-AMT >= 0 ADD DALYTRAN-AMT TO ACCT-CURR-CYC-CREDIT
        TransactionPostingJob job = new TransactionPostingJob(
                xrefByCardNum, accountsByAcctId, tranCatBalByKey);

        BigDecimal originalCredit = accountsByAcctId.get(12345678901L).getCurrCycCredit();

        TransactionRecord tran = createTransaction(
                "TRAN000000000006", "01", 1, "Online",
                "Credit test", new BigDecimal("100.00"),
                "4111111111111111", "2026-01-15-10.30.00.000000");

        job.execute(List.of(tran));

        AccountRecord updated = job.getAccountsByAcctId().get(12345678901L);
        assertEquals(originalCredit.add(new BigDecimal("100.00")), updated.getCurrCycCredit());
    }

    @Test
    void testDebitCycleUpdatedForNegativeAmount() {
        // COBOL: IF DALYTRAN-AMT < 0 ADD DALYTRAN-AMT TO ACCT-CURR-CYC-DEBIT
        TransactionPostingJob job = new TransactionPostingJob(
                xrefByCardNum, accountsByAcctId, tranCatBalByKey);

        BigDecimal originalDebit = accountsByAcctId.get(12345678901L).getCurrCycDebit();

        TransactionRecord tran = createTransaction(
                "TRAN000000000007", "01", 1, "Online",
                "Debit test", new BigDecimal("-50.00"),
                "4111111111111111", "2026-01-15-10.30.00.000000");

        job.execute(List.of(tran));

        AccountRecord updated = job.getAccountsByAcctId().get(12345678901L);
        assertEquals(originalDebit.add(new BigDecimal("-50.00")), updated.getCurrCycDebit());
    }

    @Test
    void testTranCatBalCreatedWhenMissing() {
        // COBOL 2700-A-CREATE-TCATBAL-REC: create new record if not exists
        TransactionPostingJob job = new TransactionPostingJob(
                xrefByCardNum, accountsByAcctId, tranCatBalByKey);

        TransactionRecord tran = createTransaction(
                "TRAN000000000008", "02", 3, "Online",
                "New catbal", new BigDecimal("150.00"),
                "4111111111111111", "2026-01-15-10.30.00.000000");

        job.execute(List.of(tran));

        String key = String.format("%011d%2s%04d", 12345678901L, "02", 3);
        TranCatBalRecord catBal = job.getTranCatBalByKey().get(key);
        assertNotNull(catBal, "TranCatBal record should be created");
        assertEquals(new BigDecimal("150.00"), catBal.getBalance());
    }

    @Test
    void testTranCatBalUpdatedWhenExists() {
        // COBOL 2700-B-UPDATE-TCATBAL-REC: update existing record
        String key = String.format("%011d%2s%04d", 12345678901L, "01", 1);
        tranCatBalByKey.put(key, new TranCatBalRecord(12345678901L, "01", 1, new BigDecimal("500.00")));

        TransactionPostingJob job = new TransactionPostingJob(
                xrefByCardNum, accountsByAcctId, tranCatBalByKey);

        TransactionRecord tran = createTransaction(
                "TRAN000000000009", "01", 1, "Online",
                "Update catbal", new BigDecimal("200.00"),
                "4111111111111111", "2026-01-15-10.30.00.000000");

        job.execute(List.of(tran));

        TranCatBalRecord catBal = job.getTranCatBalByKey().get(key);
        assertEquals(new BigDecimal("700.00"), catBal.getBalance());
    }

    @Test
    void testMultipleTransactionsMixedResults() {
        TransactionPostingJob job = new TransactionPostingJob(
                xrefByCardNum, accountsByAcctId, tranCatBalByKey);

        List<TransactionRecord> transactions = new ArrayList<>();
        // Valid
        transactions.add(createTransaction("T001", "01", 1, "Online",
                "OK", new BigDecimal("100.00"), "4111111111111111", "2026-01-15-10.30.00.000000"));
        // Invalid card
        transactions.add(createTransaction("T002", "01", 1, "Online",
                "Bad card", new BigDecimal("50.00"), "0000000000000000", "2026-01-15-10.30.00.000000"));
        // Valid
        transactions.add(createTransaction("T003", "01", 2, "POS",
                "OK2", new BigDecimal("75.00"), "4111111111111111", "2026-01-15-10.30.00.000000"));

        int rc = job.execute(transactions);

        assertEquals(4, rc, "Return code 4 because there are rejects");
        assertEquals(3, job.getTransactionCount());
        assertEquals(1, job.getRejectCount());
        assertEquals(2, job.getPostedTransactions().size());
        assertEquals(1, job.getRejectedTransactions().size());
    }

    private TransactionRecord createTransaction(String tranId, String typeCd, int catCd,
                                                 String source, String desc, BigDecimal amount,
                                                 String cardNum, String origTs) {
        TransactionRecord tran = new TransactionRecord();
        tran.setTranId(tranId);
        tran.setTypeCd(typeCd);
        tran.setCatCd(catCd);
        tran.setSource(source);
        tran.setDescription(desc);
        tran.setAmount(amount);
        tran.setCardNum(cardNum);
        tran.setOrigTimestamp(origTs);
        return tran;
    }
}
