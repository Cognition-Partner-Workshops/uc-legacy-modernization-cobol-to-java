package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.ArrayAccountRecord;
import com.carddemo.batch.cbact01c.model.ArrayAccountRecord.BalanceDebitPair;
import com.carddemo.batch.cbact01c.model.OutAccountRecord;
import com.carddemo.batch.cbact01c.model.VbrRecord1;
import com.carddemo.batch.cbact01c.model.VbrRecord2;
import com.carddemo.batch.cbact01c.service.AccountProcessor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AccountProcessor}, verifying that the Java transformations
 * produce results identical to the COBOL paragraphs 1300, 1400, and 1500.
 */
class AccountProcessorTest {

    /**
     * Sample account matching the first record in acctdata.txt:
     * Account 00000000001, balance 194.00, credit limit 2020.00, etc.
     * PIC S9(10)V99 = 12 digits, e.g. 00000001940{ = 194.00
     */
    private static final AccountRecord SAMPLE_ACCT_1 = new AccountRecord(
            1L,
            "Y",
            new BigDecimal("194.00"),
            new BigDecimal("2020.00"),
            new BigDecimal("1020.00"),
            "2014-11-20",
            "2025-05-20",
            "2025-05-20",
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),   // zero → should trigger default substitution
            "A000000000",
            ""
    );

    /**
     * Account with a non-zero cycle debit (should NOT be replaced with 2525.00).
     */
    private static final AccountRecord SAMPLE_ACCT_NON_ZERO_DEBIT = new AccountRecord(
            2L,
            "Y",
            new BigDecimal("158.00"),
            new BigDecimal("6130.00"),
            new BigDecimal("5448.00"),
            "2013-06-19",
            "2024-08-11",
            "2024-08-11",
            new BigDecimal("100.00"),
            new BigDecimal("50.00"),   // non-zero → kept as-is
            "12345",
            "B000000000"
    );

    // ---------------------------------------------------------------
    // 1300-POPUL-ACCT-RECORD tests
    // ---------------------------------------------------------------

    @Test
    void toOutRecord_shouldConvertReissueDateToYyyymmdd() {
        OutAccountRecord out = AccountProcessor.toOutRecord(SAMPLE_ACCT_1);
        // COBOL: YYYY-MM-DD → YYYYMMDD, padded to 10 chars
        assertEquals("20250520  ", out.reissueDate());
    }

    @Test
    void toOutRecord_shouldSubstituteDefaultDebitWhenZero() {
        OutAccountRecord out = AccountProcessor.toOutRecord(SAMPLE_ACCT_1);
        // COBOL: IF ACCT-CURR-CYC-DEBIT = ZERO MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
        assertEquals(new BigDecimal("2525.00"), out.currCycDebit());
    }

    @Test
    void toOutRecord_shouldPreserveNonZeroDebit() {
        OutAccountRecord out = AccountProcessor.toOutRecord(SAMPLE_ACCT_NON_ZERO_DEBIT);
        assertEquals(new BigDecimal("50.00"), out.currCycDebit());
    }

    @Test
    void toOutRecord_shouldCopyFieldsDirectly() {
        OutAccountRecord out = AccountProcessor.toOutRecord(SAMPLE_ACCT_1);
        assertEquals(1L, out.acctId());
        assertEquals("Y", out.activeStatus());
        assertEquals(new BigDecimal("194.00"), out.currBal());
        assertEquals(new BigDecimal("2020.00"), out.creditLimit());
        assertEquals(new BigDecimal("1020.00"), out.cashCreditLimit());
        assertEquals("2014-11-20", out.openDate());
        assertEquals("2025-05-20", out.expirationDate());
        assertEquals(new BigDecimal("0.00"), out.currCycCredit());
        assertEquals("", out.groupId());
    }

    // ---------------------------------------------------------------
    // 1400-POPUL-ARRAY-RECORD tests
    // ---------------------------------------------------------------

    @Test
    void toArrayRecord_shouldHaveFiveElements() {
        ArrayAccountRecord arr = AccountProcessor.toArrayRecord(SAMPLE_ACCT_1);
        assertEquals(ArrayAccountRecord.OCCURS_COUNT, arr.balanceDebitPairs().size());
    }

    @Test
    void toArrayRecord_element1_shouldUseActualBalanceAndFixedDebit() {
        ArrayAccountRecord arr = AccountProcessor.toArrayRecord(SAMPLE_ACCT_1);
        BalanceDebitPair pair = arr.balanceDebitPairs().get(0);
        assertEquals(new BigDecimal("194.00"), pair.currBal());
        assertEquals(new BigDecimal("1005.00"), pair.currCycDebit());
    }

    @Test
    void toArrayRecord_element2_shouldUseActualBalanceAndFixedDebit() {
        ArrayAccountRecord arr = AccountProcessor.toArrayRecord(SAMPLE_ACCT_1);
        BalanceDebitPair pair = arr.balanceDebitPairs().get(1);
        assertEquals(new BigDecimal("194.00"), pair.currBal());
        assertEquals(new BigDecimal("1525.00"), pair.currCycDebit());
    }

    @Test
    void toArrayRecord_element3_shouldUseHardCodedNegativeValues() {
        ArrayAccountRecord arr = AccountProcessor.toArrayRecord(SAMPLE_ACCT_1);
        BalanceDebitPair pair = arr.balanceDebitPairs().get(2);
        assertEquals(new BigDecimal("-1025.00"), pair.currBal());
        assertEquals(new BigDecimal("-2500.00"), pair.currCycDebit());
    }

    @Test
    void toArrayRecord_elements4And5_shouldBeZero() {
        ArrayAccountRecord arr = AccountProcessor.toArrayRecord(SAMPLE_ACCT_1);
        for (int i = 3; i < 5; i++) {
            BalanceDebitPair pair = arr.balanceDebitPairs().get(i);
            assertEquals(BigDecimal.ZERO, pair.currBal(),
                    "Element " + (i + 1) + " balance should be zero");
            assertEquals(BigDecimal.ZERO, pair.currCycDebit(),
                    "Element " + (i + 1) + " debit should be zero");
        }
    }

    @Test
    void toArrayRecord_shouldSetAccountId() {
        ArrayAccountRecord arr = AccountProcessor.toArrayRecord(SAMPLE_ACCT_1);
        assertEquals(1L, arr.acctId());
    }

    // ---------------------------------------------------------------
    // 1500-POPUL-VBRC-RECORD tests
    // ---------------------------------------------------------------

    @Test
    void toVbrRecord1_shouldContainIdAndStatus() {
        VbrRecord1 vb1 = AccountProcessor.toVbrRecord1(SAMPLE_ACCT_1);
        assertEquals(1L, vb1.acctId());
        assertEquals("Y", vb1.activeStatus());
    }

    @Test
    void toVbrRecord2_shouldContainIdBalanceLimitAndYear() {
        VbrRecord2 vb2 = AccountProcessor.toVbrRecord2(SAMPLE_ACCT_1);
        assertEquals(1L, vb2.acctId());
        assertEquals(new BigDecimal("194.00"), vb2.currBal());
        assertEquals(new BigDecimal("2020.00"), vb2.creditLimit());
        assertEquals("2025", vb2.reissueYear());
    }

    @Test
    void toVbrRecord2_shouldExtractCorrectYear() {
        VbrRecord2 vb2 = AccountProcessor.toVbrRecord2(SAMPLE_ACCT_NON_ZERO_DEBIT);
        assertEquals("2024", vb2.reissueYear());
    }
}
