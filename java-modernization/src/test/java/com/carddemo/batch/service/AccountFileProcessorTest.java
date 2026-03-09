package com.carddemo.batch.service;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VariableLengthRecord1;
import com.carddemo.batch.model.VariableLengthRecord2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AccountFileProcessor}, the core business logic that mirrors
 * COBOL paragraphs 1300, 1400, and 1500 in CBACT01C.
 */
class AccountFileProcessorTest {

    /** Account with zero cycle-debit (triggers the 2525.00 substitution rule). */
    private AccountRecord zeroDebitAccount;

    /** Account with non-zero cycle-debit. */
    private AccountRecord nonZeroDebitAccount;

    @BeforeEach
    void setUp() {
        zeroDebitAccount = new AccountRecord(
                1L, "Y",
                new BigDecimal("194.00"),
                new BigDecimal("2020.00"),
                new BigDecimal("1020.00"),
                "2014-11-20",
                "2025-05-20",
                "2025-05-20",
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),  // zero → should become 2525.00
                "",
                "A000000000"
        );

        nonZeroDebitAccount = new AccountRecord(
                99L, "Y",
                new BigDecimal("500.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("5000.00"),
                "2020-01-15",
                "2030-01-15",
                "2029-06-30",
                new BigDecimal("150.00"),
                new BigDecimal("75.50"),  // non-zero → should stay 75.50
                "12345",
                "B000000001"
        );
    }

    // ── 1300-POPUL-ACCT-RECORD tests ────────────────────────────────────

    @Nested
    @DisplayName("buildOutputRecord (paragraph 1300)")
    class BuildOutputRecordTests {

        @Test
        @DisplayName("Reissue date converted from YYYY-MM-DD to YYYYMMDD")
        void reissueDateConverted() {
            OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(zeroDebitAccount);
            // 2025-05-20 → 20250520
            assertEquals("20250520", out.acctReissueDate());
        }

        @Test
        @DisplayName("Zero cycle-debit replaced with 2525.00")
        void zeroCycleDebitSubstituted() {
            OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(zeroDebitAccount);
            assertEquals(0, new BigDecimal("2525.00").compareTo(out.acctCurrCycDebit()));
        }

        @Test
        @DisplayName("Non-zero cycle-debit preserved as-is")
        void nonZeroCycleDebitPreserved() {
            OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(nonZeroDebitAccount);
            assertEquals(0, new BigDecimal("75.50").compareTo(out.acctCurrCycDebit()));
        }

        @Test
        @DisplayName("All other fields are passed through unchanged")
        void fieldsPassedThrough() {
            OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(zeroDebitAccount);

            assertEquals(1L, out.acctId());
            assertEquals("Y", out.acctActiveStatus());
            assertEquals(0, new BigDecimal("194.00").compareTo(out.acctCurrBal()));
            assertEquals(0, new BigDecimal("2020.00").compareTo(out.acctCreditLimit()));
            assertEquals(0, new BigDecimal("1020.00").compareTo(out.acctCashCreditLimit()));
            assertEquals("2014-11-20", out.acctOpenDate());
            assertEquals("2025-05-20", out.acctExpiraionDate());
            assertEquals(0, new BigDecimal("0.00").compareTo(out.acctCurrCycCredit()));
            assertEquals("A000000000", out.acctGroupId());
        }

        @Test
        @DisplayName("Output record toDelimitedString produces correct pipe-separated format")
        void toDelimitedString() {
            OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(zeroDebitAccount);
            String line = out.toDelimitedString();

            assertTrue(line.startsWith("00000000001|Y|"));
            assertTrue(line.contains("|20250520|"));
            assertTrue(line.endsWith("|A000000000"));
            // Should contain 10 pipe separators (11 fields)
            assertEquals(10, line.chars().filter(c -> c == '|').count());
        }
    }

    // ── 1400-POPUL-ARRAY-RECORD tests ───────────────────────────────────

    @Nested
    @DisplayName("buildArrayRecord (paragraph 1400)")
    class BuildArrayRecordTests {

        @Test
        @DisplayName("Slot 1: balance = input balance, debit = 1005.00")
        void slot1() {
            ArrayRecord arr = AccountFileProcessor.buildArrayRecord(zeroDebitAccount);
            assertEquals(0, new BigDecimal("194.00").compareTo(arr.balances()[0]));
            assertEquals(0, new BigDecimal("1005.00").compareTo(arr.cycleDebits()[0]));
        }

        @Test
        @DisplayName("Slot 2: balance = input balance, debit = 1525.00")
        void slot2() {
            ArrayRecord arr = AccountFileProcessor.buildArrayRecord(zeroDebitAccount);
            assertEquals(0, new BigDecimal("194.00").compareTo(arr.balances()[1]));
            assertEquals(0, new BigDecimal("1525.00").compareTo(arr.cycleDebits()[1]));
        }

        @Test
        @DisplayName("Slot 3: balance = -1025.00, debit = -2500.00 (hardcoded)")
        void slot3() {
            ArrayRecord arr = AccountFileProcessor.buildArrayRecord(zeroDebitAccount);
            assertEquals(0, new BigDecimal("-1025.00").compareTo(arr.balances()[2]));
            assertEquals(0, new BigDecimal("-2500.00").compareTo(arr.cycleDebits()[2]));
        }

        @Test
        @DisplayName("Slots 4 and 5 remain zero-initialised")
        void slots4And5() {
            ArrayRecord arr = AccountFileProcessor.buildArrayRecord(zeroDebitAccount);
            assertEquals(0, BigDecimal.ZERO.compareTo(arr.balances()[3]));
            assertEquals(0, BigDecimal.ZERO.compareTo(arr.cycleDebits()[3]));
            assertEquals(0, BigDecimal.ZERO.compareTo(arr.balances()[4]));
            assertEquals(0, BigDecimal.ZERO.compareTo(arr.cycleDebits()[4]));
        }

        @Test
        @DisplayName("Account ID is carried through")
        void acctId() {
            ArrayRecord arr = AccountFileProcessor.buildArrayRecord(zeroDebitAccount);
            assertEquals(1L, arr.acctId());
        }
    }

    // ── 1500-POPUL-VBRC-RECORD tests ────────────────────────────────────

    @Nested
    @DisplayName("buildVbRecord1 / buildVbRecord2 (paragraph 1500)")
    class BuildVbRecordTests {

        @Test
        @DisplayName("VB1 contains account ID and active status")
        void vb1Fields() {
            VariableLengthRecord1 vb1 = AccountFileProcessor.buildVbRecord1(zeroDebitAccount);
            assertEquals(1L, vb1.acctId());
            assertEquals("Y", vb1.acctActiveStatus());
        }

        @Test
        @DisplayName("VB2 contains account ID, balance, credit limit, and reissue year")
        void vb2Fields() {
            VariableLengthRecord2 vb2 = AccountFileProcessor.buildVbRecord2(zeroDebitAccount);
            assertEquals(1L, vb2.acctId());
            assertEquals(0, new BigDecimal("194.00").compareTo(vb2.acctCurrBal()));
            assertEquals(0, new BigDecimal("2020.00").compareTo(vb2.acctCreditLimit()));
            assertEquals("2025", vb2.acctReissueYear());
        }

        @Test
        @DisplayName("VB2 reissue year extracted from non-zero reissue date")
        void vb2ReissueYearExtracted() {
            VariableLengthRecord2 vb2 = AccountFileProcessor.buildVbRecord2(nonZeroDebitAccount);
            assertEquals("2029", vb2.acctReissueYear());
        }
    }
}
