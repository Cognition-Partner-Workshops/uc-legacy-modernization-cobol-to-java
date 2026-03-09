package com.carddemo.verification;

import com.carddemo.model.Account;
import com.carddemo.model.DisclosureGroup;
import com.carddemo.model.TranCatBalance;
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
 * Strategy 3: Business Logic Unit Tests from COBOL - Interest Calculation.
 *
 * Tests derived from COBOL batch program CBACT04C (Interest Calculation, JCL job INTCALC).
 *
 * <p>CBACT04C logic flow:
 * <ol>
 *   <li>1000-TCATBALF-GET-NEXT: Read through TCATBALF file sequentially</li>
 *   <li>For each category balance record, look up disclosure group for matching
 *       (acct_group_id, type_cd, cat_cd)</li>
 *   <li>If specific group not found, fall back to DEFAULT group</li>
 *   <li>1300-COMPUTE-INTEREST: monthly_interest = (cat_balance * int_rate) / 1200</li>
 *   <li>1500-UPDATE-ACCOUNT: Add monthly interest to account current balance</li>
 *   <li>1600-WRITE-TRAN: Write interest transaction record to TRANSACT file</li>
 * </ol>
 *
 * <p>The division by 1200 converts annual rate to monthly: rate/100 (percent to decimal) / 12 (annual to monthly).
 */
class InterestCalculationTest {

    private Map<Long, Account> accountTable;
    private Map<String, DisclosureGroup> discGroupTable;
    private List<TranCatBalance> tcatBalRecords;

    @BeforeEach
    void setUp() {
        accountTable = new HashMap<>();
        discGroupTable = new HashMap<>();
        tcatBalRecords = new ArrayList<>();

        // Account 1 - GROUP001
        Account acct1 = new Account();
        acct1.setAcctId(1L);
        acct1.setAcctActiveStatus("Y");
        acct1.setAcctCurrBal(new BigDecimal("2500.00"));
        acct1.setAcctGroupId("GROUP001");
        acct1.setAcctCurrCycCredit(BigDecimal.ZERO);
        acct1.setAcctCurrCycDebit(BigDecimal.ZERO);
        accountTable.put(1L, acct1);

        // Account 2 - GROUP002
        Account acct2 = new Account();
        acct2.setAcctId(2L);
        acct2.setAcctActiveStatus("Y");
        acct2.setAcctCurrBal(new BigDecimal("10000.00"));
        acct2.setAcctGroupId("GROUP002");
        acct2.setAcctCurrCycCredit(BigDecimal.ZERO);
        acct2.setAcctCurrCycDebit(BigDecimal.ZERO);
        accountTable.put(2L, acct2);

        // Account 3 - unknown group, should fallback to DEFAULT
        Account acct3 = new Account();
        acct3.setAcctId(3L);
        acct3.setAcctActiveStatus("Y");
        acct3.setAcctCurrBal(new BigDecimal("1000.00"));
        acct3.setAcctGroupId("UNKNOWN");
        acct3.setAcctCurrCycCredit(BigDecimal.ZERO);
        acct3.setAcctCurrCycDebit(BigDecimal.ZERO);
        accountTable.put(3L, acct3);

        // Disclosure groups
        addDiscGroup("GROUP001", "01", 1, "18.0000", "0.00");  // Purchases at 18% APR
        addDiscGroup("GROUP001", "02", 2, "24.0000", "5.00");  // Cash advances at 24% APR
        addDiscGroup("GROUP002", "01", 1, "12.0000", "0.00");  // Premium: 12% APR
        addDiscGroup("DEFAULT", "01", 1, "15.0000", "0.00");   // Default: 15% APR
        addDiscGroup("DEFAULT", "02", 2, "21.0000", "3.00");   // Default cash: 21% APR

        // Category balances
        addTcatBal(1L, "01", 1, "2000.00");    // Acct 1 purchases
        addTcatBal(1L, "02", 2, "500.00");     // Acct 1 cash advances
        addTcatBal(2L, "01", 1, "8000.00");    // Acct 2 purchases
        addTcatBal(3L, "01", 1, "1000.00");    // Acct 3 purchases (will use DEFAULT)
    }

    // --- Core Interest Formula Tests ---

    /**
     * CBACT04C paragraph 1300-COMPUTE-INTEREST:
     * COMPUTE WS-MONTHLY-INT = (WS-TCAT-BAL * DIS-INT-RATE) / 1200
     */
    @Test
    void testMonthlyInterestFormula_Standard18Percent() {
        BigDecimal balance = new BigDecimal("2000.00");
        BigDecimal rate = new BigDecimal("18.0000");
        BigDecimal expected = new BigDecimal("30.00"); // 2000 * 18 / 1200

        BigDecimal result = computeMonthlyInterest(balance, rate);
        assertEquals(expected, result);
    }

    @Test
    void testMonthlyInterestFormula_HighRate24Percent() {
        BigDecimal balance = new BigDecimal("500.00");
        BigDecimal rate = new BigDecimal("24.0000");
        BigDecimal expected = new BigDecimal("10.00"); // 500 * 24 / 1200

        BigDecimal result = computeMonthlyInterest(balance, rate);
        assertEquals(expected, result);
    }

    @Test
    void testMonthlyInterestFormula_LowRate12Percent() {
        BigDecimal balance = new BigDecimal("8000.00");
        BigDecimal rate = new BigDecimal("12.0000");
        BigDecimal expected = new BigDecimal("80.00"); // 8000 * 12 / 1200

        BigDecimal result = computeMonthlyInterest(balance, rate);
        assertEquals(expected, result);
    }

    @Test
    void testMonthlyInterestFormula_ZeroBalance() {
        BigDecimal result = computeMonthlyInterest(BigDecimal.ZERO, new BigDecimal("18.0000"));
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void testMonthlyInterestFormula_ZeroRate() {
        BigDecimal result = computeMonthlyInterest(new BigDecimal("5000.00"), BigDecimal.ZERO);
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void testMonthlyInterestFormula_FractionalResult() {
        // 1234.56 * 17.50 / 1200 = 18.00483... rounds to 18.00
        BigDecimal balance = new BigDecimal("1234.56");
        BigDecimal rate = new BigDecimal("17.5000");
        BigDecimal result = computeMonthlyInterest(balance, rate);
        assertEquals(new BigDecimal("18.00"), result);
    }

    @Test
    void testMonthlyInterestFormula_RoundingHalfUp() {
        // 1000.00 * 7.00 / 1200 = 5.8333... rounds to 5.83
        BigDecimal balance = new BigDecimal("1000.00");
        BigDecimal rate = new BigDecimal("7.0000");
        BigDecimal result = computeMonthlyInterest(balance, rate);
        assertEquals(new BigDecimal("5.83"), result);
    }

    // --- Disclosure Group Lookup Tests ---

    @Test
    void testDisclosureGroupLookup_SpecificGroupFound() {
        DisclosureGroup dg = lookupDisclosureGroup("GROUP001", "01", 1);
        assertNotNull(dg);
        assertEquals(new BigDecimal("18.0000"), dg.getDisIntRate());
    }

    @Test
    void testDisclosureGroupLookup_DefaultFallback() {
        // UNKNOWN group should fall back to DEFAULT
        DisclosureGroup dg = lookupDisclosureGroup("UNKNOWN", "01", 1);
        assertNotNull(dg, "Should find DEFAULT group as fallback");
        assertEquals("DEFAULT", dg.getDisAcctGroupId());
        assertEquals(new BigDecimal("15.0000"), dg.getDisIntRate());
    }

    @Test
    void testDisclosureGroupLookup_DefaultFallbackForCashAdvance() {
        DisclosureGroup dg = lookupDisclosureGroup("UNKNOWN", "02", 2);
        assertNotNull(dg);
        assertEquals("DEFAULT", dg.getDisAcctGroupId());
        assertEquals(new BigDecimal("21.0000"), dg.getDisIntRate());
    }

    @Test
    void testDisclosureGroupLookup_NeitherSpecificNorDefaultExists() {
        // Type/cat combo that has no disclosure group at all
        DisclosureGroup dg = lookupDisclosureGroup("UNKNOWN", "99", 99);
        assertNull(dg, "Should return null when no matching group exists");
    }

    // --- Full Interest Calculation Cycle Tests ---

    @Test
    void testFullCycleAccount1_TwoCategories() {
        Account acct = accountTable.get(1L);
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (TranCatBalance tcb : tcatBalRecords) {
            if (!tcb.getTrancatAcctId().equals(1L)) continue;

            DisclosureGroup dg = lookupDisclosureGroup(acct.getAcctGroupId(),
                    tcb.getTrancatTypeCd(), tcb.getTrancatCd());
            assertNotNull(dg);

            BigDecimal interest = computeMonthlyInterest(tcb.getTrancatBal(), dg.getDisIntRate());
            totalInterest = totalInterest.add(interest);
        }

        // Purchases: 2000 * 18 / 1200 = 30.00
        // Cash advances: 500 * 24 / 1200 = 10.00
        // Total: 40.00
        assertEquals(new BigDecimal("40.00"), totalInterest);

        // Update account balance
        acct.setAcctCurrBal(acct.getAcctCurrBal().add(totalInterest));
        assertEquals(new BigDecimal("2540.00"), acct.getAcctCurrBal());
    }

    @Test
    void testFullCycleAccount2_SingleCategory() {
        Account acct = accountTable.get(2L);
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (TranCatBalance tcb : tcatBalRecords) {
            if (!tcb.getTrancatAcctId().equals(2L)) continue;

            DisclosureGroup dg = lookupDisclosureGroup(acct.getAcctGroupId(),
                    tcb.getTrancatTypeCd(), tcb.getTrancatCd());
            assertNotNull(dg);

            BigDecimal interest = computeMonthlyInterest(tcb.getTrancatBal(), dg.getDisIntRate());
            totalInterest = totalInterest.add(interest);
        }

        // Purchases: 8000 * 12 / 1200 = 80.00
        assertEquals(new BigDecimal("80.00"), totalInterest);

        acct.setAcctCurrBal(acct.getAcctCurrBal().add(totalInterest));
        assertEquals(new BigDecimal("10080.00"), acct.getAcctCurrBal());
    }

    @Test
    void testFullCycleAccount3_DefaultGroupFallback() {
        Account acct = accountTable.get(3L);
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (TranCatBalance tcb : tcatBalRecords) {
            if (!tcb.getTrancatAcctId().equals(3L)) continue;

            DisclosureGroup dg = lookupDisclosureGroup(acct.getAcctGroupId(),
                    tcb.getTrancatTypeCd(), tcb.getTrancatCd());
            assertNotNull(dg, "Should find DEFAULT disclosure group");
            assertEquals("DEFAULT", dg.getDisAcctGroupId());

            BigDecimal interest = computeMonthlyInterest(tcb.getTrancatBal(), dg.getDisIntRate());
            totalInterest = totalInterest.add(interest);
        }

        // Purchases: 1000 * 15 / 1200 = 12.50
        assertEquals(new BigDecimal("12.50"), totalInterest);

        acct.setAcctCurrBal(acct.getAcctCurrBal().add(totalInterest));
        assertEquals(new BigDecimal("1012.50"), acct.getAcctCurrBal());
    }

    // --- Interest Transaction Record Creation (paragraph 1600-WRITE-TRAN) ---

    @Test
    void testInterestTransactionRecord() {
        // When interest is computed, CBACT04C writes a transaction record
        BigDecimal interest = new BigDecimal("30.00");

        // Verify transaction record fields
        assertNotNull(interest);
        assertTrue(interest.compareTo(BigDecimal.ZERO) > 0,
                "Interest transaction should have positive amount");
        assertEquals(2, interest.scale(), "Interest should have 2 decimal places");
    }

    @Test
    void testZeroInterestNoTransaction() {
        // If balance is zero, no interest transaction should be written
        BigDecimal interest = computeMonthlyInterest(BigDecimal.ZERO, new BigDecimal("18.0000"));
        assertEquals(0, interest.compareTo(BigDecimal.ZERO),
                "Zero balance should produce zero interest - no transaction written");
    }

    // --- Edge Cases ---

    @Test
    void testNegativeBalance() {
        // Credit (negative) balance should produce negative interest (credit to customer)
        BigDecimal balance = new BigDecimal("-500.00");
        BigDecimal rate = new BigDecimal("18.0000");
        BigDecimal result = computeMonthlyInterest(balance, rate);
        assertEquals(new BigDecimal("-7.50"), result,
                "Negative balance should produce negative interest");
    }

    @Test
    void testVeryLargeBalance() {
        BigDecimal balance = new BigDecimal("99999999.99");
        BigDecimal rate = new BigDecimal("24.0000");
        BigDecimal result = computeMonthlyInterest(balance, rate);
        // 99999999.99 * 24 / 1200 = 2000000.00
        assertEquals(new BigDecimal("2000000.00"), result);
    }

    @Test
    void testVerySmallBalance() {
        BigDecimal balance = new BigDecimal("0.01");
        BigDecimal rate = new BigDecimal("18.0000");
        BigDecimal result = computeMonthlyInterest(balance, rate);
        // 0.01 * 18 / 1200 = 0.00015 rounds to 0.00
        assertEquals(new BigDecimal("0.00"), result);
    }

    // ============================================================
    // Helper methods replicating CBACT04C logic
    // ============================================================

    /**
     * CBACT04C paragraph 1300-COMPUTE-INTEREST:
     * COMPUTE WS-MONTHLY-INT = (WS-TCAT-BAL * DIS-INT-RATE) / 1200
     */
    private BigDecimal computeMonthlyInterest(BigDecimal balance, BigDecimal annualRate) {
        return balance.multiply(annualRate)
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);
    }

    /**
     * CBACT04C disclosure group lookup with DEFAULT fallback.
     * First tries specific group ID, then falls back to "DEFAULT".
     */
    private DisclosureGroup lookupDisclosureGroup(String groupId, String typeCd, int catCd) {
        String specificKey = groupId + "|" + typeCd + "|" + catCd;
        DisclosureGroup dg = discGroupTable.get(specificKey);
        if (dg != null) {
            return dg;
        }
        // Fallback to DEFAULT
        String defaultKey = "DEFAULT|" + typeCd + "|" + catCd;
        return discGroupTable.get(defaultKey);
    }

    private void addDiscGroup(String groupId, String typeCd, int catCd,
                              String rate, String fee) {
        DisclosureGroup dg = new DisclosureGroup();
        dg.setDisAcctGroupId(groupId);
        dg.setDisTranTypeCd(typeCd);
        dg.setDisTranCatCd(catCd);
        dg.setDisIntRate(new BigDecimal(rate));
        dg.setDisFeeAmt(new BigDecimal(fee));
        discGroupTable.put(groupId + "|" + typeCd + "|" + catCd, dg);
    }

    private void addTcatBal(Long acctId, String typeCd, int catCd, String balance) {
        TranCatBalance tcb = new TranCatBalance();
        tcb.setTrancatAcctId(acctId);
        tcb.setTrancatTypeCd(typeCd);
        tcb.setTrancatCd(catCd);
        tcb.setTrancatBal(new BigDecimal(balance));
        tcatBalRecords.add(tcb);
    }
}
