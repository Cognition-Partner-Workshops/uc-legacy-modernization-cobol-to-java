package com.carddemo.verification;

import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.DisclosureGroup;
import com.carddemo.model.TranCatBalance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Strategy 2: Golden Output / Regression Testing.
 *
 * Implements reference test datasets derived from COBOL batch program logic
 * (CBTRN02C for transaction posting, CBACT04C for interest calculation).
 * Each test creates known input data, applies the same business rules as the
 * COBOL program, and verifies the Java output matches the expected COBOL output.
 *
 * Since the batch jobs are Phase 2 stubs, these tests validate the business
 * rules directly (not through Spring Batch) to serve as regression baselines
 * when the full batch implementation is completed.
 */
class GoldenOutputRegressionTest {

    // --- Reference data derived from COBOL CBTRN02C logic ---

    private Map<String, CardXref> xrefTable;
    private Map<Long, Account> accountTable;

    @BeforeEach
    void setUp() {
        // Seed reference data matching COBOL VSAM file contents
        xrefTable = new HashMap<>();
        accountTable = new HashMap<>();

        // Account 1: Active, within credit limit, not expired
        Account acct1 = new Account();
        acct1.setAcctId(1L);
        acct1.setAcctActiveStatus("Y");
        acct1.setAcctCurrBal(new BigDecimal("500.00"));
        acct1.setAcctCreditLimit(new BigDecimal("5000.00"));
        acct1.setAcctCashCreditLimit(new BigDecimal("1500.00"));
        acct1.setAcctOpenDate("2020-01-15");
        acct1.setAcctExpirationDate("2027-12-31");
        acct1.setAcctReissueDate("2025-01-15");
        acct1.setAcctCurrCycCredit(BigDecimal.ZERO);
        acct1.setAcctCurrCycDebit(BigDecimal.ZERO);
        acct1.setAcctGroupId("GROUP001");
        accountTable.put(1L, acct1);

        // Account 2: Active but over credit limit
        Account acct2 = new Account();
        acct2.setAcctId(2L);
        acct2.setAcctActiveStatus("Y");
        acct2.setAcctCurrBal(new BigDecimal("4900.00"));
        acct2.setAcctCreditLimit(new BigDecimal("5000.00"));
        acct2.setAcctCashCreditLimit(new BigDecimal("1500.00"));
        acct2.setAcctOpenDate("2019-06-01");
        acct2.setAcctExpirationDate("2027-06-30");
        acct2.setAcctReissueDate("2024-06-01");
        acct2.setAcctCurrCycCredit(BigDecimal.ZERO);
        acct2.setAcctCurrCycDebit(BigDecimal.ZERO);
        acct2.setAcctGroupId("GROUP001");
        accountTable.put(2L, acct2);

        // Account 3: Expired
        Account acct3 = new Account();
        acct3.setAcctId(3L);
        acct3.setAcctActiveStatus("Y");
        acct3.setAcctCurrBal(new BigDecimal("100.00"));
        acct3.setAcctCreditLimit(new BigDecimal("3000.00"));
        acct3.setAcctCashCreditLimit(new BigDecimal("1000.00"));
        acct3.setAcctOpenDate("2018-01-01");
        acct3.setAcctExpirationDate("2024-12-31");
        acct3.setAcctReissueDate("2022-01-01");
        acct3.setAcctCurrCycCredit(BigDecimal.ZERO);
        acct3.setAcctCurrCycDebit(BigDecimal.ZERO);
        acct3.setAcctGroupId("GROUP002");
        accountTable.put(3L, acct3);

        // Card xref entries
        CardXref xref1 = new CardXref();
        xref1.setXrefCardNum("4111111111111111");
        xref1.setXrefCustId(1L);
        xref1.setXrefAcctId(1L);
        xrefTable.put("4111111111111111", xref1);

        CardXref xref2 = new CardXref();
        xref2.setXrefCardNum("4222222222222222");
        xref2.setXrefCustId(2L);
        xref2.setXrefAcctId(2L);
        xrefTable.put("4222222222222222", xref2);

        CardXref xref3 = new CardXref();
        xref3.setXrefCardNum("4333333333333333");
        xref3.setXrefCustId(3L);
        xref3.setXrefAcctId(3L);
        xrefTable.put("4333333333333333", xref3);
    }

    // ============================================================
    // CBTRN02C Transaction Posting - Golden Output Tests
    // ============================================================

    /**
     * CBTRN02C paragraph 1500-A-LOOKUP-XREF:
     * If card not in XREFFILE, validation_fail_reason = 100.
     */
    @Test
    void testTransactionPostingRejectInvalidXref() {
        String cardNum = "9999999999999999"; // Not in xref
        int validationResult = validateTransaction(cardNum, new BigDecimal("50.00"), "2026-03-01");
        assertEquals(100, validationResult, "Missing xref should produce error code 100");
    }

    /**
     * CBTRN02C paragraph 1500-B-LOOKUP-ACCT:
     * If account not found for the xref acct_id, validation_fail_reason = 101.
     */
    @Test
    void testTransactionPostingRejectMissingAccount() {
        // Add xref pointing to non-existent account
        CardXref badXref = new CardXref();
        badXref.setXrefCardNum("5555555555555555");
        badXref.setXrefCustId(99L);
        badXref.setXrefAcctId(999L); // Account 999 does not exist
        xrefTable.put("5555555555555555", badXref);

        int validationResult = validateTransaction("5555555555555555", new BigDecimal("50.00"), "2026-03-01");
        assertEquals(101, validationResult, "Missing account should produce error code 101");
    }

    /**
     * CBTRN02C credit limit check:
     * If curr_bal + tran_amt > credit_limit, validation_fail_reason = 102.
     */
    @Test
    void testTransactionPostingRejectOverlimit() {
        // Account 2 has bal=4900, limit=5000. Transaction of 200 would exceed.
        int validationResult = validateTransaction("4222222222222222", new BigDecimal("200.00"), "2026-03-01");
        assertEquals(102, validationResult, "Over-limit transaction should produce error code 102");
    }

    /**
     * CBTRN02C expiration check:
     * If account expiration date < transaction date, validation_fail_reason = 103.
     */
    @Test
    void testTransactionPostingRejectExpired() {
        // Account 3 expired 2024-12-31, transaction date is 2026-03-01
        int validationResult = validateTransaction("4333333333333333", new BigDecimal("50.00"), "2026-03-01");
        assertEquals(103, validationResult, "Expired account should produce error code 103");
    }

    /**
     * CBTRN02C: Valid transaction should pass all checks (result = 0).
     */
    @Test
    void testTransactionPostingValidTransaction() {
        // Account 1: active, within limit, not expired
        int validationResult = validateTransaction("4111111111111111", new BigDecimal("100.00"), "2026-03-01");
        assertEquals(0, validationResult, "Valid transaction should produce no error (code 0)");
    }

    /**
     * CBTRN02C paragraph 2800-UPDATE-ACCOUNT-REC:
     * After posting, account balance should be updated.
     */
    @Test
    void testTransactionPostingBalanceUpdate() {
        Account acct = accountTable.get(1L);
        BigDecimal originalBal = acct.getAcctCurrBal();
        BigDecimal tranAmt = new BigDecimal("100.00");

        // Simulate posting - COBOL adds tran amount to current balance
        BigDecimal newBal = originalBal.add(tranAmt);
        acct.setAcctCurrBal(newBal);
        acct.setAcctCurrCycDebit(acct.getAcctCurrCycDebit().add(tranAmt));

        assertEquals(new BigDecimal("600.00"), acct.getAcctCurrBal(),
                "Balance should be updated after posting");
        assertEquals(new BigDecimal("100.00"), acct.getAcctCurrCycDebit(),
                "Cycle debit should reflect transaction amount");
    }

    /**
     * CBTRN02C: Batch of mixed transactions - count valid and rejected.
     * Golden output: given 5 transactions, expect 2 valid + 3 rejected.
     */
    @Test
    void testBatchTransactionGoldenOutput() {
        int validCount = 0;
        int rejectCount = 0;

        // Transaction 1: valid (account 1, within limit, not expired)
        if (validateTransaction("4111111111111111", new BigDecimal("100.00"), "2026-03-01") == 0) {
            validCount++;
        } else {
            rejectCount++;
        }

        // Transaction 2: reject - bad xref
        if (validateTransaction("9999999999999999", new BigDecimal("50.00"), "2026-03-01") == 0) {
            validCount++;
        } else {
            rejectCount++;
        }

        // Transaction 3: reject - over limit (account 2: 4900 + 200 > 5000)
        if (validateTransaction("4222222222222222", new BigDecimal("200.00"), "2026-03-01") == 0) {
            validCount++;
        } else {
            rejectCount++;
        }

        // Transaction 4: reject - expired account
        if (validateTransaction("4333333333333333", new BigDecimal("50.00"), "2026-03-01") == 0) {
            validCount++;
        } else {
            rejectCount++;
        }

        // Transaction 5: valid (account 1, small amount)
        if (validateTransaction("4111111111111111", new BigDecimal("25.00"), "2026-03-01") == 0) {
            validCount++;
        } else {
            rejectCount++;
        }

        assertEquals(2, validCount, "Golden output: 2 valid transactions");
        assertEquals(3, rejectCount, "Golden output: 3 rejected transactions");
    }

    // ============================================================
    // CBACT04C Interest Calculation - Golden Output Tests
    // ============================================================

    /**
     * CBACT04C paragraph 1300-COMPUTE-INTEREST:
     * monthlyInt = (catBalance * rate) / 1200
     * (rate is annual percentage, divide by 1200 for monthly decimal)
     */
    @Test
    void testInterestCalculationStandardRate() {
        BigDecimal catBalance = new BigDecimal("1000.00");
        BigDecimal annualRate = new BigDecimal("18.00"); // 18% APR

        // COBOL: COMPUTE WS-MONTHLY-INT = (WS-TCAT-BAL * DIS-INT-RATE) / 1200
        BigDecimal monthlyInt = catBalance.multiply(annualRate)
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal("15.00"), monthlyInt,
                "Monthly interest on $1000 at 18% APR = $15.00");
    }

    @Test
    void testInterestCalculationLowRate() {
        BigDecimal catBalance = new BigDecimal("5000.00");
        BigDecimal annualRate = new BigDecimal("12.00"); // 12% APR

        BigDecimal monthlyInt = catBalance.multiply(annualRate)
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal("50.00"), monthlyInt,
                "Monthly interest on $5000 at 12% APR = $50.00");
    }

    @Test
    void testInterestCalculationZeroRate() {
        BigDecimal catBalance = new BigDecimal("1000.00");
        BigDecimal annualRate = BigDecimal.ZERO;

        BigDecimal monthlyInt = catBalance.multiply(annualRate)
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);

        assertEquals(0, monthlyInt.compareTo(BigDecimal.ZERO),
                "Zero rate should produce zero interest");
    }

    @Test
    void testInterestCalculationZeroBalance() {
        BigDecimal catBalance = BigDecimal.ZERO;
        BigDecimal annualRate = new BigDecimal("18.00");

        BigDecimal monthlyInt = catBalance.multiply(annualRate)
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);

        assertEquals(0, monthlyInt.compareTo(BigDecimal.ZERO),
                "Zero balance should produce zero interest");
    }

    /**
     * CBACT04C: Full account interest calculation with multiple category balances.
     * Golden output: account with 2 categories should sum interest correctly.
     */
    @Test
    void testFullAccountInterestGoldenOutput() {
        // Category balance 1: Purchase balance at 18% APR
        TranCatBalance tcb1 = new TranCatBalance();
        tcb1.setTrancatAcctId(1L);
        tcb1.setTrancatTypeCd("01");
        tcb1.setTrancatCd(1);
        tcb1.setTrancatBal(new BigDecimal("1000.00"));

        // Category balance 2: Cash advance balance at 24% APR
        TranCatBalance tcb2 = new TranCatBalance();
        tcb2.setTrancatAcctId(1L);
        tcb2.setTrancatTypeCd("02");
        tcb2.setTrancatCd(2);
        tcb2.setTrancatBal(new BigDecimal("500.00"));

        // Disclosure groups with rates
        DisclosureGroup dg1 = new DisclosureGroup();
        dg1.setDisAcctGroupId("GROUP001");
        dg1.setDisTranTypeCd("01");
        dg1.setDisTranCatCd(1);
        dg1.setDisIntRate(new BigDecimal("18.00"));

        DisclosureGroup dg2 = new DisclosureGroup();
        dg2.setDisAcctGroupId("GROUP001");
        dg2.setDisTranTypeCd("02");
        dg2.setDisTranCatCd(2);
        dg2.setDisIntRate(new BigDecimal("24.00"));

        // Calculate interest per CBACT04C logic
        BigDecimal int1 = tcb1.getTrancatBal().multiply(dg1.getDisIntRate())
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);
        BigDecimal int2 = tcb2.getTrancatBal().multiply(dg2.getDisIntRate())
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);
        BigDecimal totalInterest = int1.add(int2);

        // Golden output: 1000*18/1200 = 15.00 + 500*24/1200 = 10.00 = 25.00
        assertEquals(new BigDecimal("15.00"), int1, "Purchase interest = $15.00");
        assertEquals(new BigDecimal("10.00"), int2, "Cash advance interest = $10.00");
        assertEquals(new BigDecimal("25.00"), totalInterest, "Total interest = $25.00");

        // Apply to account balance
        Account acct = accountTable.get(1L);
        BigDecimal originalBal = acct.getAcctCurrBal();
        acct.setAcctCurrBal(originalBal.add(totalInterest));

        assertEquals(new BigDecimal("525.00"), acct.getAcctCurrBal(),
                "Account balance after interest: 500.00 + 25.00 = 525.00");
    }

    /**
     * CBACT04C: DEFAULT disclosure group fallback.
     * When specific group not found, use DEFAULT group.
     */
    @Test
    void testInterestCalculationDefaultGroupFallback() {
        // Simulate lookup: specific group not found, fall back to DEFAULT
        String acctGroupId = "UNKNOWN_G";
        String typeCd = "01";
        int catCd = 1;

        // No specific disclosure group for UNKNOWN_G
        DisclosureGroup specificGroup = lookupDisclosureGroup(acctGroupId, typeCd, catCd);
        assertNull(specificGroup, "Specific group should not exist");

        // Fall back to DEFAULT group
        DisclosureGroup defaultGroup = lookupDisclosureGroup("DEFAULT", typeCd, catCd);
        assertNotNull(defaultGroup, "DEFAULT group should exist as fallback");
        assertEquals(new BigDecimal("15.00"), defaultGroup.getDisIntRate());
    }

    // ============================================================
    // CBTRN02C paragraph 2700-UPDATE-TCATBAL
    // ============================================================

    @Test
    void testTranCatBalanceUpdateExisting() {
        TranCatBalance tcb = new TranCatBalance();
        tcb.setTrancatAcctId(1L);
        tcb.setTrancatTypeCd("01");
        tcb.setTrancatCd(1);
        tcb.setTrancatBal(new BigDecimal("500.00"));

        BigDecimal tranAmt = new BigDecimal("100.00");

        // COBOL: ADD WS-TRAN-AMT TO WS-TCAT-BAL
        tcb.setTrancatBal(tcb.getTrancatBal().add(tranAmt));

        assertEquals(new BigDecimal("600.00"), tcb.getTrancatBal(),
                "Category balance should be updated with transaction amount");
    }

    @Test
    void testTranCatBalanceCreateNew() {
        // New category balance record - COBOL creates it if not found
        TranCatBalance newTcb = new TranCatBalance();
        newTcb.setTrancatAcctId(1L);
        newTcb.setTrancatTypeCd("03");
        newTcb.setTrancatCd(3);
        newTcb.setTrancatBal(BigDecimal.ZERO);

        BigDecimal tranAmt = new BigDecimal("75.00");
        newTcb.setTrancatBal(newTcb.getTrancatBal().add(tranAmt));

        assertEquals(new BigDecimal("75.00"), newTcb.getTrancatBal(),
                "New category balance should equal first transaction amount");
    }

    // ============================================================
    // Helper methods replicating COBOL validation logic
    // ============================================================

    /**
     * Replicates CBTRN02C validation flow:
     * 1. Lookup xref (error 100 if not found)
     * 2. Lookup account (error 101 if not found)
     * 3. Check credit limit (error 102 if exceeded)
     * 4. Check expiration (error 103 if expired)
     *
     * @return 0 if valid, or error code (100-103)
     */
    private int validateTransaction(String cardNum, BigDecimal tranAmt, String tranDate) {
        // Step 1: XREF lookup (CBTRN02C paragraph 1500-A-LOOKUP-XREF)
        CardXref xref = xrefTable.get(cardNum);
        if (xref == null) {
            return 100; // Card not in cross-reference
        }

        // Step 2: Account lookup (CBTRN02C paragraph 1500-B-LOOKUP-ACCT)
        Account acct = accountTable.get(xref.getXrefAcctId());
        if (acct == null) {
            return 101; // Account not found
        }

        // Step 3: Credit limit check
        BigDecimal newBal = acct.getAcctCurrBal().add(tranAmt);
        if (newBal.compareTo(acct.getAcctCreditLimit()) > 0) {
            return 102; // Over credit limit
        }

        // Step 4: Expiration check
        if (acct.getAcctExpirationDate() != null && acct.getAcctExpirationDate().compareTo(tranDate) < 0) {
            return 103; // Account expired
        }

        return 0; // Valid
    }

    /**
     * Simulates CBACT04C disclosure group lookup with DEFAULT fallback.
     */
    private DisclosureGroup lookupDisclosureGroup(String groupId, String typeCd, int catCd) {
        // Simulate a small lookup table
        Map<String, DisclosureGroup> discGroups = new HashMap<>();

        DisclosureGroup dg1 = new DisclosureGroup();
        dg1.setDisAcctGroupId("GROUP001");
        dg1.setDisTranTypeCd("01");
        dg1.setDisTranCatCd(1);
        dg1.setDisIntRate(new BigDecimal("18.00"));
        discGroups.put("GROUP001|01|1", dg1);

        DisclosureGroup dgDefault = new DisclosureGroup();
        dgDefault.setDisAcctGroupId("DEFAULT");
        dgDefault.setDisTranTypeCd("01");
        dgDefault.setDisTranCatCd(1);
        dgDefault.setDisIntRate(new BigDecimal("15.00"));
        discGroups.put("DEFAULT|01|1", dgDefault);

        String key = groupId + "|" + typeCd + "|" + catCd;
        return discGroups.get(key);
    }
}
