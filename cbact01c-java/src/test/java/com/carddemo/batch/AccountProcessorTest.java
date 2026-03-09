package com.carddemo.batch;

import com.carddemo.batch.model.*;
import com.carddemo.batch.service.AccountProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AccountProcessor — the core business logic of CBACT01C.
 *
 * <p>Each test verifies that the Java transformation produces identical
 * results to what the COBOL program would produce for the same input.
 */
class AccountProcessorTest {

    private AccountProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new AccountProcessor();
    }

    // ---------------------------------------------------------------
    // Helper to build sample AccountRecords matching acctdata.txt
    // ---------------------------------------------------------------

    /** Account #1 from acctdata.txt — cycle debit is ZERO (triggers default). */
    private AccountRecord account1() {
        return new AccountRecord(
                1L, "Y",
                new BigDecimal("194.00"),
                new BigDecimal("2020.00"),
                new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                "A000000000", "          "
        );
    }

    /** Account #2 from acctdata.txt — cycle debit is ZERO. */
    private AccountRecord account2() {
        return new AccountRecord(
                2L, "Y",
                new BigDecimal("158.00"),
                new BigDecimal("6130.00"),
                new BigDecimal("5448.00"),
                "2013-06-19", "2024-08-11", "2024-08-11",
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                "A000000000", "          "
        );
    }

    /** Account #3 from acctdata.txt — cycle debit is ZERO. */
    private AccountRecord account3() {
        return new AccountRecord(
                3L, "Y",
                new BigDecimal("147.00"),
                new BigDecimal("4909.00"),
                new BigDecimal("538.00"),
                "2013-08-23", "2024-01-10", "2024-01-10",
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                "A000000000", "          "
        );
    }

    /** Synthetic account with non-zero cycle debit (no default applied). */
    private AccountRecord accountWithNonZeroDebit() {
        return new AccountRecord(
                99L, "Y",
                new BigDecimal("5000.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("3000.00"),
                "2020-01-01", "2025-12-31", "2025-06-15",
                new BigDecimal("100.00"),
                new BigDecimal("750.50"),
                "B000000000", "G000000001"
        );
    }

    // ===================================================================
    // 1300-POPUL-ACCT-RECORD — OutAccountRecord tests
    // ===================================================================

    @Nested
    class ToOutRecordTests {

        @Test
        void account1_dateConvertedToYyyymmdd() {
            OutAccountRecord out = processor.toOutRecord(account1());
            // COBOL: MOVE ACCT-REISSUE-DATE TO CODATECN-INP-DATE
            //        MOVE '2' TO CODATECN-TYPE (YYYY-MM-DD input)
            //        MOVE '2' TO CODATECN-OUTTYPE (YYYYMMDD output)
            //        CALL 'COBDATFT' -> "20250520"
            assertTrue(out.reissueDate().trim().startsWith("20250520"),
                    "Expected YYYYMMDD conversion of 2025-05-20, got: " + out.reissueDate());
        }

        @Test
        void account1_zeroDebitReplacedWithDefault() {
            // COBOL: IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
            //            MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
            OutAccountRecord out = processor.toOutRecord(account1());
            assertEquals(new BigDecimal("2525.00"), out.currCycDebit());
        }

        @Test
        void account2_dateConvertedCorrectly() {
            OutAccountRecord out = processor.toOutRecord(account2());
            assertTrue(out.reissueDate().trim().startsWith("20240811"));
        }

        @Test
        void account2_zeroDebitReplacedWithDefault() {
            OutAccountRecord out = processor.toOutRecord(account2());
            assertEquals(new BigDecimal("2525.00"), out.currCycDebit());
        }

        @Test
        void account3_dateConvertedCorrectly() {
            OutAccountRecord out = processor.toOutRecord(account3());
            assertTrue(out.reissueDate().trim().startsWith("20240110"));
        }

        @Test
        void nonZeroDebitPreserved() {
            // When ACCT-CURR-CYC-DEBIT is NOT zero, it should pass through unchanged
            OutAccountRecord out = processor.toOutRecord(accountWithNonZeroDebit());
            assertEquals(new BigDecimal("750.50"), out.currCycDebit());
        }

        @Test
        void allFieldsMappedCorrectly() {
            AccountRecord in = account1();
            OutAccountRecord out = processor.toOutRecord(in);

            assertEquals(in.acctId(), out.acctId());
            assertEquals(in.activeStatus(), out.activeStatus());
            assertEquals(in.currBal(), out.currBal());
            assertEquals(in.creditLimit(), out.creditLimit());
            assertEquals(in.cashCreditLimit(), out.cashCreditLimit());
            assertEquals(in.openDate(), out.openDate());
            assertEquals(in.expirationDate(), out.expirationDate());
            assertEquals(in.currCycCredit(), out.currCycCredit());
            assertEquals(in.groupId(), out.groupId());
        }
    }

    // ===================================================================
    // 1400-POPUL-ARRAY-RECORD — ArrayRecord tests
    // ===================================================================

    @Nested
    class ToArrayRecordTests {

        @Test
        void hasExactlyFiveEntries() {
            ArrayRecord arr = processor.toArrayRecord(account1());
            assertEquals(5, arr.entries().length);
        }

        @Test
        void entry1_balanceIsActual_debitIs1005() {
            // COBOL: MOVE ACCT-CURR-BAL TO ARR-ACCT-CURR-BAL(1)
            //        MOVE 1005.00       TO ARR-ACCT-CURR-CYC-DEBIT(1)
            ArrayRecord arr = processor.toArrayRecord(account1());
            assertEquals(new BigDecimal("194.00"), arr.entries()[0].currBal());
            assertEquals(new BigDecimal("1005.00"), arr.entries()[0].currCycDebit());
        }

        @Test
        void entry2_balanceIsActual_debitIs1525() {
            // COBOL: MOVE ACCT-CURR-BAL TO ARR-ACCT-CURR-BAL(2)
            //        MOVE 1525.00       TO ARR-ACCT-CURR-CYC-DEBIT(2)
            ArrayRecord arr = processor.toArrayRecord(account1());
            assertEquals(new BigDecimal("194.00"), arr.entries()[1].currBal());
            assertEquals(new BigDecimal("1525.00"), arr.entries()[1].currCycDebit());
        }

        @Test
        void entry3_hardcodedNegativeValues() {
            // COBOL: MOVE -1025.00 TO ARR-ACCT-CURR-BAL(3)
            //        MOVE -2500.00 TO ARR-ACCT-CURR-CYC-DEBIT(3)
            ArrayRecord arr = processor.toArrayRecord(account1());
            assertEquals(new BigDecimal("-1025.00"), arr.entries()[2].currBal());
            assertEquals(new BigDecimal("-2500.00"), arr.entries()[2].currCycDebit());
        }

        @Test
        void entries4And5_areZero() {
            // After INITIALIZE, entries 4-5 remain at zero
            ArrayRecord arr = processor.toArrayRecord(account1());
            assertEquals(BigDecimal.ZERO, arr.entries()[3].currBal());
            assertEquals(BigDecimal.ZERO, arr.entries()[3].currCycDebit());
            assertEquals(BigDecimal.ZERO, arr.entries()[4].currBal());
            assertEquals(BigDecimal.ZERO, arr.entries()[4].currCycDebit());
        }

        @Test
        void account2_usesCorrectBalance() {
            ArrayRecord arr = processor.toArrayRecord(account2());
            assertEquals(new BigDecimal("158.00"), arr.entries()[0].currBal());
            assertEquals(new BigDecimal("158.00"), arr.entries()[1].currBal());
        }

        @Test
        void accountIdMapped() {
            ArrayRecord arr = processor.toArrayRecord(account1());
            assertEquals(1L, arr.acctId());
        }
    }

    // ===================================================================
    // 1500-POPUL-VBRC-RECORD — VbRecord1 / VbRecord2 tests
    // ===================================================================

    @Nested
    class ToVbRecordTests {

        @Test
        void vb1_accountIdAndStatus() {
            // COBOL: MOVE ACCT-ID TO VB1-ACCT-ID
            //        MOVE ACCT-ACTIVE-STATUS TO VB1-ACCT-ACTIVE-STATUS
            VbRecord1 vb1 = processor.toVbRecord1(account1());
            assertEquals(1L, vb1.acctId());
            assertEquals("Y", vb1.activeStatus());
        }

        @Test
        void vb2_accountIdBalanceLimitAndYear() {
            // COBOL: MOVE ACCT-ID TO VB2-ACCT-ID
            //        MOVE ACCT-CURR-BAL TO VB2-ACCT-CURR-BAL
            //        MOVE ACCT-CREDIT-LIMIT TO VB2-ACCT-CREDIT-LIMIT
            //        MOVE WS-ACCT-REISSUE-YYYY TO VB2-ACCT-REISSUE-YYYY
            VbRecord2 vb2 = processor.toVbRecord2(account1());
            assertEquals(1L, vb2.acctId());
            assertEquals(new BigDecimal("194.00"), vb2.currBal());
            assertEquals(new BigDecimal("2020.00"), vb2.creditLimit());
            assertEquals("2025", vb2.reissueYear());
        }

        @Test
        void vb2_account2_yearExtracted() {
            VbRecord2 vb2 = processor.toVbRecord2(account2());
            assertEquals("2024", vb2.reissueYear());
        }

        @Test
        void vb2_account3_yearExtracted() {
            VbRecord2 vb2 = processor.toVbRecord2(account3());
            assertEquals("2024", vb2.reissueYear());
        }

        @Test
        void vb1_account2() {
            VbRecord1 vb1 = processor.toVbRecord1(account2());
            assertEquals(2L, vb1.acctId());
            assertEquals("Y", vb1.activeStatus());
        }
    }

    // ===================================================================
    // Batch processing
    // ===================================================================

    @Nested
    class BatchProcessingTests {

        @Test
        void processBatchReturnsCorrectCounts() {
            var accounts = java.util.List.of(account1(), account2(), account3());
            var result = processor.processBatch(accounts);

            assertEquals(3, result.outRecords().size());
            assertEquals(3, result.arrayRecords().size());
            assertEquals(3, result.vbRecords1().size());
            assertEquals(3, result.vbRecords2().size());
        }

        @Test
        void processBatchAllDebitDefaulted() {
            // All three sample accounts have zero debit -> all should get 2525.00
            var accounts = java.util.List.of(account1(), account2(), account3());
            var result = processor.processBatch(accounts);

            for (OutAccountRecord out : result.outRecords()) {
                assertEquals(new BigDecimal("2525.00"), out.currCycDebit(),
                        "Account " + out.acctId() + " should have default debit 2525.00");
            }
        }
    }
}
