package com.carddemo.batch;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.OutAccountRecord;
import com.carddemo.batch.model.VbrcRecord1;
import com.carddemo.batch.model.VbrcRecord2;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the core business logic of the AccountProcessor, verifying that
 * the Java version produces identical transformation results to the
 * COBOL CBACT01C program for known sample inputs.
 */
class AccountProcessorTest {

    // ---------------------------------------------------------------
    // Sample account record #1 (from acctdata.txt line 1)
    // ---------------------------------------------------------------
    private static final AccountRecord ACCT_1 = new AccountRecord(
            1L, "Y",
            new BigDecimal("194.00"),
            new BigDecimal("2020.00"),
            new BigDecimal("1020.00"),
            "2014-11-20", "2025-05-20", "2025-05-20",
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            "A000000000", "          "
    );

    // Sample account record #2 (from acctdata.txt line 2)
    private static final AccountRecord ACCT_2 = new AccountRecord(
            2L, "Y",
            new BigDecimal("158.00"),
            new BigDecimal("6130.00"),
            new BigDecimal("5448.00"),
            "2013-06-19", "2024-08-11", "2024-08-11",
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            "A000000000", "          "
    );

    // Account with nonzero debit — to test the bypass of the 2525.00 default
    private static final AccountRecord ACCT_WITH_DEBIT = new AccountRecord(
            99L, "Y",
            new BigDecimal("500.00"),
            new BigDecimal("1000.00"),
            new BigDecimal("800.00"),
            "2020-01-15", "2025-01-15", "2025-01-15",
            new BigDecimal("100.00"),
            new BigDecimal("75.50"),
            "12345", "B000000001"
    );

    // ---------------------------------------------------------------
    // 1300-POPUL-ACCT-RECORD tests
    // ---------------------------------------------------------------

    @Test
    void testPopulateOutRecord_zeroCycDebitSubstituted() {
        // COBOL rule: if ACCT-CURR-CYC-DEBIT == 0, move 2525.00
        OutAccountRecord out = AccountProcessor.populateOutRecord(ACCT_1);

        assertEquals(1L, out.acctId());
        assertEquals("Y", out.activeStatus());
        assertEquals(new BigDecimal("194.00"), out.currBal());
        assertEquals(new BigDecimal("2020.00"), out.creditLimit());
        assertEquals(new BigDecimal("1020.00"), out.cashCreditLimit());
        assertEquals("2014-11-20", out.openDate());
        assertEquals("2025-05-20", out.expiraionDate());
        // Reissue date converted: "2025-05-20" -> "20250520" + 2 spaces
        assertEquals("20250520  ", out.reissueDate());
        assertEquals(new BigDecimal("0.00"), out.currCycCredit());
        // Zero debit replaced with 2525.00
        assertEquals(new BigDecimal("2525.00"), out.currCycDebit());
        assertEquals("          ", out.groupId());
    }

    @Test
    void testPopulateOutRecord_nonzeroCycDebitPreserved() {
        OutAccountRecord out = AccountProcessor.populateOutRecord(ACCT_WITH_DEBIT);

        assertEquals(99L, out.acctId());
        // Non-zero debit should be preserved, NOT replaced with 2525.00
        assertEquals(new BigDecimal("75.50"), out.currCycDebit());
        assertEquals(new BigDecimal("100.00"), out.currCycCredit());
        // Reissue date converted
        assertEquals("20250115  ", out.reissueDate());
    }

    @Test
    void testPopulateOutRecord_dateConversion() {
        // Verify the COBDATFT replacement: YYYY-MM-DD -> YYYYMMDD
        OutAccountRecord out = AccountProcessor.populateOutRecord(ACCT_2);
        assertEquals("20240811  ", out.reissueDate());
    }

    // ---------------------------------------------------------------
    // 1400-POPUL-ARRAY-RECORD tests
    // ---------------------------------------------------------------

    @Test
    void testPopulateArrayRecord_structure() {
        ArrayRecord arr = AccountProcessor.populateArrayRecord(ACCT_1);

        assertEquals(1L, arr.acctId());
        assertEquals(5, arr.balanceEntries().size());

        // Index 0: currBal=ACCT-CURR-BAL, debit=1005.00
        assertEquals(new BigDecimal("194.00"), arr.balanceEntries().get(0).currBal());
        assertEquals(new BigDecimal("1005.00"), arr.balanceEntries().get(0).currCycDebit());

        // Index 1: currBal=ACCT-CURR-BAL, debit=1525.00
        assertEquals(new BigDecimal("194.00"), arr.balanceEntries().get(1).currBal());
        assertEquals(new BigDecimal("1525.00"), arr.balanceEntries().get(1).currCycDebit());

        // Index 2: currBal=-1025.00, debit=-2500.00
        assertEquals(new BigDecimal("-1025.00"), arr.balanceEntries().get(2).currBal());
        assertEquals(new BigDecimal("-2500.00"), arr.balanceEntries().get(2).currCycDebit());

        // Index 3 & 4: zeroed (from INITIALIZE)
        assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(3).currBal());
        assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(3).currCycDebit());
        assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(4).currBal());
        assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(4).currCycDebit());
    }

    @Test
    void testPopulateArrayRecord_differentBalance() {
        ArrayRecord arr = AccountProcessor.populateArrayRecord(ACCT_2);

        // Indices 0 and 1 should reflect ACCT_2's balance
        assertEquals(new BigDecimal("158.00"), arr.balanceEntries().get(0).currBal());
        assertEquals(new BigDecimal("158.00"), arr.balanceEntries().get(1).currBal());

        // Indices 2-4 are always the same fixed values
        assertEquals(new BigDecimal("-1025.00"), arr.balanceEntries().get(2).currBal());
    }

    // ---------------------------------------------------------------
    // 1500-POPUL-VBRC-RECORD tests
    // ---------------------------------------------------------------

    @Test
    void testPopulateVbrcRecord1() {
        VbrcRecord1 vb1 = AccountProcessor.populateVbrcRecord1(ACCT_1);

        assertEquals(1L, vb1.acctId());
        assertEquals("Y", vb1.activeStatus());
    }

    @Test
    void testPopulateVbrcRecord2() {
        VbrcRecord2 vb2 = AccountProcessor.populateVbrcRecord2(ACCT_1);

        assertEquals(1L, vb2.acctId());
        assertEquals(new BigDecimal("194.00"), vb2.currBal());
        assertEquals(new BigDecimal("2020.00"), vb2.creditLimit());
        // Extract YYYY from "2025-05-20"
        assertEquals("2025", vb2.reissueYyyy());
    }

    @Test
    void testPopulateVbrcRecord2_differentAccount() {
        VbrcRecord2 vb2 = AccountProcessor.populateVbrcRecord2(ACCT_2);

        assertEquals(2L, vb2.acctId());
        assertEquals(new BigDecimal("158.00"), vb2.currBal());
        assertEquals(new BigDecimal("6130.00"), vb2.creditLimit());
        assertEquals("2024", vb2.reissueYyyy());
    }
}
