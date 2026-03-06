package com.carddemo.batch.service;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord;
import com.carddemo.batch.model.BalanceEntry;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VbrcRecord1;
import com.carddemo.batch.model.VbrcRecord2;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link AccountProcessor}, verifying that the business logic
 * from COBOL paragraphs 1300, 1400, and 1500 is faithfully reproduced.
 */
class AccountProcessorTest {

    /**
     * Creates a sample account record matching the first line of acctdata.txt.
     * All cycle debit/credit values are zero, which triggers the 2525.00 rule.
     */
    private static AccountRecord sampleAccount1() {
        return new AccountRecord(
                1L, "Y",
                new BigDecimal("194.00"),
                new BigDecimal("2020.00"),
                new BigDecimal("1020.00"),
                "2014-11-20",
                "2025-05-20",
                "2025-05-20",
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                "A000000000",
                ""
        );
    }

    /**
     * Account with a non-zero cycle debit to verify the substitution is NOT applied.
     */
    private static AccountRecord sampleAccountNonZeroDebit() {
        return new AccountRecord(
                42L, "Y",
                new BigDecimal("302.00"),
                new BigDecimal("6563.00"),
                new BigDecimal("5103.00"),
                "2016-09-19",
                "2025-09-19",
                "2025-09-19",
                new BigDecimal("0.00"),
                new BigDecimal("150.75"),
                "",
                ""
        );
    }

    // ========================================================================
    // 1300-POPUL-ACCT-RECORD tests
    // ========================================================================

    @Test
    void buildOutputRecord_convertsReissueDate_dashedToCompact() {
        OutputAccountRecord out = AccountProcessor.buildOutputRecord(sampleAccount1());
        // COBOL: YYYY-MM-DD -> YYYYMMDD (type 2 -> type 2)
        assertEquals("20250520", out.reissueDate());
    }

    @Test
    void buildOutputRecord_substitutesZeroDebit_with2525() {
        OutputAccountRecord out = AccountProcessor.buildOutputRecord(sampleAccount1());
        // COBOL: IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO MOVE 2525.00
        assertEquals(new BigDecimal("2525.00"), out.currentCycleDebit());
    }

    @Test
    void buildOutputRecord_preservesNonZeroDebit() {
        OutputAccountRecord out = AccountProcessor.buildOutputRecord(sampleAccountNonZeroDebit());
        assertEquals(new BigDecimal("150.75"), out.currentCycleDebit());
    }

    @Test
    void buildOutputRecord_copiesAllFieldsCorrectly() {
        AccountRecord input = sampleAccount1();
        OutputAccountRecord out = AccountProcessor.buildOutputRecord(input);

        assertEquals(input.acctId(), out.acctId());
        assertEquals(input.activeStatus(), out.activeStatus());
        assertEquals(input.currentBalance(), out.currentBalance());
        assertEquals(input.creditLimit(), out.creditLimit());
        assertEquals(input.cashCreditLimit(), out.cashCreditLimit());
        assertEquals(input.openDate(), out.openDate());
        assertEquals(input.expirationDate(), out.expirationDate());
        assertEquals(input.currentCycleCredit(), out.currentCycleCredit());
        assertEquals(input.groupId(), out.groupId());
    }

    @Test
    void buildOutputRecord_toOutputLine_format() {
        OutputAccountRecord out = AccountProcessor.buildOutputRecord(sampleAccount1());
        String line = out.toOutputLine();
        String[] parts = line.split("\\|", -1);

        assertEquals(11, parts.length);
        assertEquals("00000000001", parts[0]);
        assertEquals("Y", parts[1]);
        assertEquals("194.00", parts[2]);
        assertEquals("2020.00", parts[3]);
        assertEquals("1020.00", parts[4]);
        assertEquals("2014-11-20", parts[5]);
        assertEquals("2025-05-20", parts[6]);
        assertEquals("20250520", parts[7]);     // converted date
        assertEquals("0.00", parts[8]);
        assertEquals("2525.00", parts[9]);       // substituted debit
        assertEquals("", parts[10]);             // empty group ID
    }

    // ========================================================================
    // 1400-POPUL-ARRAY-RECORD tests
    // ========================================================================

    @Test
    void buildArrayRecord_element1_actualBalance_hardcodedDebit() {
        ArrayAccountRecord arr = AccountProcessor.buildArrayRecord(sampleAccount1());
        BalanceEntry e1 = arr.balanceEntries().get(0);

        assertEquals(new BigDecimal("194.00"), e1.currentBalance());
        assertEquals(new BigDecimal("1005.00"), e1.currentCycleDebit());
    }

    @Test
    void buildArrayRecord_element2_actualBalance_hardcodedDebit() {
        ArrayAccountRecord arr = AccountProcessor.buildArrayRecord(sampleAccount1());
        BalanceEntry e2 = arr.balanceEntries().get(1);

        assertEquals(new BigDecimal("194.00"), e2.currentBalance());
        assertEquals(new BigDecimal("1525.00"), e2.currentCycleDebit());
    }

    @Test
    void buildArrayRecord_element3_hardcodedValues() {
        ArrayAccountRecord arr = AccountProcessor.buildArrayRecord(sampleAccount1());
        BalanceEntry e3 = arr.balanceEntries().get(2);

        assertEquals(new BigDecimal("-1025.00"), e3.currentBalance());
        assertEquals(new BigDecimal("-2500.00"), e3.currentCycleDebit());
    }

    @Test
    void buildArrayRecord_elements4and5_areZero() {
        ArrayAccountRecord arr = AccountProcessor.buildArrayRecord(sampleAccount1());

        assertEquals(BalanceEntry.ZERO, arr.balanceEntries().get(3));
        assertEquals(BalanceEntry.ZERO, arr.balanceEntries().get(4));
    }

    @Test
    void buildArrayRecord_has5Entries() {
        ArrayAccountRecord arr = AccountProcessor.buildArrayRecord(sampleAccount1());
        assertEquals(5, arr.balanceEntries().size());
    }

    @Test
    void buildArrayRecord_acctIdMatches() {
        ArrayAccountRecord arr = AccountProcessor.buildArrayRecord(sampleAccount1());
        assertEquals(1L, arr.acctId());
    }

    // ========================================================================
    // 1500-POPUL-VBRC-RECORD tests
    // ========================================================================

    @Test
    void buildVbrcRecord1_containsIdAndStatus() {
        VbrcRecord1 vb1 = AccountProcessor.buildVbrcRecord1(sampleAccount1());

        assertEquals(1L, vb1.acctId());
        assertEquals("Y", vb1.activeStatus());
    }

    @Test
    void buildVbrcRecord2_extractsReissueYear() {
        VbrcRecord2 vb2 = AccountProcessor.buildVbrcRecord2(sampleAccount1());

        assertEquals(1L, vb2.acctId());
        assertEquals(new BigDecimal("194.00"), vb2.currentBalance());
        assertEquals(new BigDecimal("2020.00"), vb2.creditLimit());
        assertEquals("2025", vb2.reissueYear());
    }

    @Test
    void buildVbrcRecord1_toOutputLine_format() {
        VbrcRecord1 vb1 = AccountProcessor.buildVbrcRecord1(sampleAccount1());
        assertEquals("00000000001|Y", vb1.toOutputLine());
    }

    @Test
    void buildVbrcRecord2_toOutputLine_format() {
        VbrcRecord2 vb2 = AccountProcessor.buildVbrcRecord2(sampleAccount1());
        assertEquals("00000000001|194.00|2020.00|2025", vb2.toOutputLine());
    }

    // ========================================================================
    // Multi-record consistency
    // ========================================================================

    @Test
    void nonZeroDebitAccount_arrayRecordUsesActualBalance() {
        AccountRecord input = sampleAccountNonZeroDebit();
        ArrayAccountRecord arr = AccountProcessor.buildArrayRecord(input);

        // Elements 1 and 2 should use the account's actual current balance
        assertEquals(new BigDecimal("302.00"), arr.balanceEntries().get(0).currentBalance());
        assertEquals(new BigDecimal("302.00"), arr.balanceEntries().get(1).currentBalance());
    }

    @Test
    void nonZeroDebitAccount_vb2_reissueYear() {
        VbrcRecord2 vb2 = AccountProcessor.buildVbrcRecord2(sampleAccountNonZeroDebit());
        assertEquals("2025", vb2.reissueYear());
    }
}
