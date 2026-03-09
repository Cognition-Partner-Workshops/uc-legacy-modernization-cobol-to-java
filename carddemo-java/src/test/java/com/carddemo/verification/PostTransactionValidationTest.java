package com.carddemo.verification;

import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.TranCatBalance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Strategy 3: Business Logic Unit Tests from COBOL - Transaction Posting.
 *
 * Tests derived from COBOL batch program CBTRN02C (Post Transactions, JCL job POSTTRAN).
 *
 * <p>CBTRN02C validation logic paragraphs:
 * <ul>
 *   <li>1500-A-LOOKUP-XREF: READ XREFFILE using card number as key</li>
 *   <li>1500-B-LOOKUP-ACCT: READ ACCTFILE using xref account ID</li>
 *   <li>Credit limit check: IF ACCT-CURR-BAL + TRAN-AMT > ACCT-CREDIT-LIMIT</li>
 *   <li>Expiration check: IF ACCT-EXPIRATION-DATE < TRAN-DATE</li>
 *   <li>2700-UPDATE-TCATBAL: Update category balance (create if new)</li>
 *   <li>2800-UPDATE-ACCOUNT-REC: Update account balance and cycle totals</li>
 * </ul>
 *
 * <p>Error codes:
 * <ul>
 *   <li>100: Card number not found in cross-reference file</li>
 *   <li>101: Account not found for cross-referenced account ID</li>
 *   <li>102: Transaction would exceed account credit limit</li>
 *   <li>103: Account card has expired</li>
 * </ul>
 */
class PostTransactionValidationTest {

    private Map<String, CardXref> xrefTable;
    private Map<Long, Account> accountTable;
    private Map<String, TranCatBalance> tcatBalTable;

    @BeforeEach
    void setUp() {
        xrefTable = new HashMap<>();
        accountTable = new HashMap<>();
        tcatBalTable = new HashMap<>();

        // Standard active account
        Account acct1 = createAccount(1L, "Y", "1000.00", "10000.00", "2000.00",
                "2020-01-01", "2028-12-31");
        accountTable.put(1L, acct1);

        // Account near credit limit
        Account acct2 = createAccount(2L, "Y", "9900.00", "10000.00", "2000.00",
                "2019-06-01", "2027-06-30");
        accountTable.put(2L, acct2);

        // Expired account
        Account acct3 = createAccount(3L, "Y", "500.00", "5000.00", "1000.00",
                "2018-01-01", "2024-12-31");
        accountTable.put(3L, acct3);

        // Inactive account
        Account acct4 = createAccount(4L, "N", "200.00", "3000.00", "500.00",
                "2020-01-01", "2027-12-31");
        accountTable.put(4L, acct4);

        // Xref entries
        xrefTable.put("4111111111111111", createXref("4111111111111111", 1L, 1L));
        xrefTable.put("4222222222222222", createXref("4222222222222222", 2L, 2L));
        xrefTable.put("4333333333333333", createXref("4333333333333333", 3L, 3L));
        xrefTable.put("4444444444444444", createXref("4444444444444444", 4L, 4L));
    }

    // --- Error Code 100: XREF Not Found ---

    @Test
    void testError100_CardNotInXref() {
        int result = validateTransaction("9999999999999999", new BigDecimal("50.00"),
                "01", 1, "2026-03-01");
        assertEquals(100, result, "Unknown card should produce error 100");
    }

    @Test
    void testError100_EmptyCardNumber() {
        int result = validateTransaction("", new BigDecimal("50.00"),
                "01", 1, "2026-03-01");
        assertEquals(100, result, "Empty card number should produce error 100");
    }

    // --- Error Code 101: Account Not Found ---

    @Test
    void testError101_AccountNotFound() {
        // Xref pointing to non-existent account
        xrefTable.put("5555555555555555", createXref("5555555555555555", 99L, 999L));

        int result = validateTransaction("5555555555555555", new BigDecimal("50.00"),
                "01", 1, "2026-03-01");
        assertEquals(101, result, "Missing account should produce error 101");
    }

    // --- Error Code 102: Over Credit Limit ---

    @Test
    void testError102_ExactlyAtLimit() {
        // Account 2: bal=9900, limit=10000. Transaction of 100 = exactly at limit.
        int result = validateTransaction("4222222222222222", new BigDecimal("100.00"),
                "01", 1, "2026-03-01");
        assertEquals(0, result, "Exactly at limit should be accepted (not over)");
    }

    @Test
    void testError102_OneOverLimit() {
        // Account 2: bal=9900, limit=10000. Transaction of 100.01 = over limit.
        int result = validateTransaction("4222222222222222", new BigDecimal("100.01"),
                "01", 1, "2026-03-01");
        assertEquals(102, result, "One cent over limit should produce error 102");
    }

    @Test
    void testError102_LargeOverLimit() {
        int result = validateTransaction("4222222222222222", new BigDecimal("5000.00"),
                "01", 1, "2026-03-01");
        assertEquals(102, result, "Large overlimit transaction should produce error 102");
    }

    // --- Error Code 103: Expired Account ---

    @Test
    void testError103_ExpiredAccount() {
        // Account 3: expires 2024-12-31, tran date 2026-03-01
        int result = validateTransaction("4333333333333333", new BigDecimal("10.00"),
                "01", 1, "2026-03-01");
        assertEquals(103, result, "Expired account should produce error 103");
    }

    @Test
    void testError103_ExpirationDateEqualsTransactionDate() {
        // Account 3: expires 2024-12-31, tran date is same
        int result = validateTransaction("4333333333333333", new BigDecimal("10.00"),
                "01", 1, "2024-12-31");
        assertEquals(0, result, "Transaction on exact expiration date should be accepted");
    }

    // --- Valid Transactions ---

    @Test
    void testValidTransaction_StandardPurchase() {
        int result = validateTransaction("4111111111111111", new BigDecimal("250.00"),
                "01", 1, "2026-03-01");
        assertEquals(0, result, "Standard purchase within limits should be valid");
    }

    @Test
    void testValidTransaction_SmallAmount() {
        int result = validateTransaction("4111111111111111", new BigDecimal("0.01"),
                "01", 1, "2026-03-01");
        assertEquals(0, result, "Minimum amount transaction should be valid");
    }

    @Test
    void testValidTransaction_ZeroAmount() {
        int result = validateTransaction("4111111111111111", BigDecimal.ZERO,
                "01", 1, "2026-03-01");
        assertEquals(0, result, "Zero amount transaction should pass validation");
    }

    // --- Account Balance Update (paragraph 2800-UPDATE-ACCOUNT-REC) ---

    @Test
    void testAccountBalanceUpdateAfterPosting() {
        Account acct = accountTable.get(1L);
        BigDecimal originalBal = acct.getAcctCurrBal();
        BigDecimal tranAmt = new BigDecimal("250.00");

        // COBOL: ADD WS-TRAN-AMT TO ACCT-CURR-BAL
        acct.setAcctCurrBal(originalBal.add(tranAmt));
        // COBOL: ADD WS-TRAN-AMT TO ACCT-CURR-CYC-DEBIT
        acct.setAcctCurrCycDebit(acct.getAcctCurrCycDebit().add(tranAmt));

        assertEquals(new BigDecimal("1250.00"), acct.getAcctCurrBal());
        assertEquals(new BigDecimal("250.00"), acct.getAcctCurrCycDebit());
    }

    @Test
    void testMultipleTransactionsAccumulateBalance() {
        Account acct = accountTable.get(1L);
        BigDecimal[] amounts = {
                new BigDecimal("100.00"),
                new BigDecimal("200.00"),
                new BigDecimal("50.00")
        };

        for (BigDecimal amt : amounts) {
            acct.setAcctCurrBal(acct.getAcctCurrBal().add(amt));
            acct.setAcctCurrCycDebit(acct.getAcctCurrCycDebit().add(amt));
        }

        assertEquals(new BigDecimal("1350.00"), acct.getAcctCurrBal(),
                "Balance after 3 transactions: 1000 + 100 + 200 + 50 = 1350");
        assertEquals(new BigDecimal("350.00"), acct.getAcctCurrCycDebit(),
                "Cycle debit should sum all transaction amounts");
    }

    // --- Category Balance Update (paragraph 2700-UPDATE-TCATBAL) ---

    @Test
    void testTcatBalUpdateExistingRecord() {
        TranCatBalance tcb = new TranCatBalance();
        tcb.setTrancatAcctId(1L);
        tcb.setTrancatTypeCd("01");
        tcb.setTrancatCd(1);
        tcb.setTrancatBal(new BigDecimal("500.00"));

        String key = "1|01|1";
        tcatBalTable.put(key, tcb);

        // COBOL: READ TCATBALF, ADD WS-TRAN-AMT TO TCAT-BAL, REWRITE
        BigDecimal tranAmt = new BigDecimal("100.00");
        TranCatBalance existing = tcatBalTable.get(key);
        existing.setTrancatBal(existing.getTrancatBal().add(tranAmt));

        assertEquals(new BigDecimal("600.00"), existing.getTrancatBal());
    }

    @Test
    void testTcatBalCreateNewRecord() {
        // COBOL: WRITE new TCATBALF record when not found for this category
        String key = "1|03|3";
        assertNull(tcatBalTable.get(key), "Should not exist initially");

        TranCatBalance newTcb = new TranCatBalance();
        newTcb.setTrancatAcctId(1L);
        newTcb.setTrancatTypeCd("03");
        newTcb.setTrancatCd(3);
        newTcb.setTrancatBal(new BigDecimal("75.00"));
        tcatBalTable.put(key, newTcb);

        assertNotNull(tcatBalTable.get(key));
        assertEquals(new BigDecimal("75.00"), tcatBalTable.get(key).getTrancatBal());
    }

    // --- Validation Priority Order ---

    @Test
    void testValidationErrorPriority_XrefBeforeAccount() {
        // Both xref and account would fail, but xref is checked first
        int result = validateTransaction("9999999999999999", new BigDecimal("50.00"),
                "01", 1, "2026-03-01");
        assertEquals(100, result, "XREF check (100) should come before account check (101)");
    }

    @Test
    void testValidationErrorPriority_LimitBeforeExpiration() {
        // Create account that is both over-limit AND expired
        Account overlimitExpired = createAccount(5L, "Y", "9999.00", "10000.00", "500.00",
                "2018-01-01", "2024-12-31");
        accountTable.put(5L, overlimitExpired);
        xrefTable.put("6666666666666666", createXref("6666666666666666", 5L, 5L));

        // Transaction of 200 would put over limit
        int result = validateTransaction("6666666666666666", new BigDecimal("200.00"),
                "01", 1, "2026-03-01");
        assertEquals(102, result, "Credit limit check (102) should come before expiration check (103)");
    }

    // ============================================================
    // Helper methods
    // ============================================================

    private int validateTransaction(String cardNum, BigDecimal tranAmt,
                                    String typeCd, int catCd, String tranDate) {
        // Step 1: XREF lookup
        CardXref xref = xrefTable.get(cardNum);
        if (xref == null) {
            return 100;
        }

        // Step 2: Account lookup
        Account acct = accountTable.get(xref.getXrefAcctId());
        if (acct == null) {
            return 101;
        }

        // Step 3: Credit limit check
        BigDecimal newBal = acct.getAcctCurrBal().add(tranAmt);
        if (newBal.compareTo(acct.getAcctCreditLimit()) > 0) {
            return 102;
        }

        // Step 4: Expiration check
        if (acct.getAcctExpirationDate() != null && acct.getAcctExpirationDate().compareTo(tranDate) < 0) {
            return 103;
        }

        return 0;
    }

    private Account createAccount(Long id, String status, String bal, String limit,
                                  String cashLimit, String openDate, String expDate) {
        Account acct = new Account();
        acct.setAcctId(id);
        acct.setAcctActiveStatus(status);
        acct.setAcctCurrBal(new BigDecimal(bal));
        acct.setAcctCreditLimit(new BigDecimal(limit));
        acct.setAcctCashCreditLimit(new BigDecimal(cashLimit));
        acct.setAcctOpenDate(openDate);
        acct.setAcctExpirationDate(expDate);
        acct.setAcctCurrCycCredit(BigDecimal.ZERO);
        acct.setAcctCurrCycDebit(BigDecimal.ZERO);
        return acct;
    }

    private CardXref createXref(String cardNum, Long custId, Long acctId) {
        CardXref xref = new CardXref();
        xref.setXrefCardNum(cardNum);
        xref.setXrefCustId(custId);
        xref.setXrefAcctId(acctId);
        return xref;
    }
}
