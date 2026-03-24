package com.carddemo.batch.service;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.OutAccountRecord;
import com.carddemo.batch.model.VbrRecord1;
import com.carddemo.batch.model.VbrRecord2;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountProcessorTest {

    private static AccountRecord sampleAccount(BigDecimal cycDebit) {
        return new AccountRecord(
                "00000000001", "Y",
                new BigDecimal("194.00"),
                new BigDecimal("2020.00"),
                new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                new BigDecimal("0.00"),
                cycDebit,
                "",
                "A000000000"
        );
    }

    // ---- OutAccountRecord tests (1300-POPUL-ACCT-RECORD) ----

    @Test
    void buildOutRecord_copiesFieldsDirectly() {
        AccountRecord acct = sampleAccount(new BigDecimal("100.00"));
        OutAccountRecord out = AccountProcessor.buildOutRecord(acct);

        assertEquals("00000000001", out.acctId());
        assertEquals("Y", out.acctActiveStatus());
        assertEquals(new BigDecimal("194.00"), out.acctCurrBal());
        assertEquals(new BigDecimal("2020.00"), out.acctCreditLimit());
        assertEquals(new BigDecimal("1020.00"), out.acctCashCreditLimit());
        assertEquals("2014-11-20", out.acctOpenDate());
        assertEquals("2025-05-20", out.acctExpiraionDate());
        assertEquals("A000000000", out.acctGroupId());
    }

    @Test
    void buildOutRecord_reformatsReissueDate() {
        AccountRecord acct = sampleAccount(new BigDecimal("100.00"));
        OutAccountRecord out = AccountProcessor.buildOutRecord(acct);
        // YYYY-MM-DD -> YYYYMMDD via COBDATFT
        assertEquals("20250520", out.acctReissueDate());
    }

    @Test
    void buildOutRecord_zeroDebit_replacedWith2525() {
        // COBOL: IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO MOVE 2525.00
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        OutAccountRecord out = AccountProcessor.buildOutRecord(acct);
        assertEquals(new BigDecimal("2525.00"), out.acctCurrCycDebit());
    }

    @Test
    void buildOutRecord_nonZeroDebit_keptAsIs() {
        AccountRecord acct = sampleAccount(new BigDecimal("500.00"));
        OutAccountRecord out = AccountProcessor.buildOutRecord(acct);
        assertEquals(new BigDecimal("500.00"), out.acctCurrCycDebit());
    }

    @Test
    void buildOutRecord_negativeDebit_keptAsIs() {
        AccountRecord acct = sampleAccount(new BigDecimal("-250.00"));
        OutAccountRecord out = AccountProcessor.buildOutRecord(acct);
        assertEquals(new BigDecimal("-250.00"), out.acctCurrCycDebit());
    }

    // ---- ArrayRecord tests (1400-POPUL-ARRAY-RECORD) ----

    @Test
    void buildArrayRecord_correctAccountId() {
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        ArrayRecord arr = AccountProcessor.buildArrayRecord(acct);
        assertEquals("00000000001", arr.acctId());
    }

    @Test
    void buildArrayRecord_slot1_balanceFromAccount_debit1005() {
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        ArrayRecord arr = AccountProcessor.buildArrayRecord(acct);
        assertEquals(new BigDecimal("194.00"), arr.balanceEntries().get(0).acctCurrBal());
        assertEquals(new BigDecimal("1005.00"), arr.balanceEntries().get(0).acctCurrCycDebit());
    }

    @Test
    void buildArrayRecord_slot2_balanceFromAccount_debit1525() {
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        ArrayRecord arr = AccountProcessor.buildArrayRecord(acct);
        assertEquals(new BigDecimal("194.00"), arr.balanceEntries().get(1).acctCurrBal());
        assertEquals(new BigDecimal("1525.00"), arr.balanceEntries().get(1).acctCurrCycDebit());
    }

    @Test
    void buildArrayRecord_slot3_fixedNegativeValues() {
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        ArrayRecord arr = AccountProcessor.buildArrayRecord(acct);
        assertEquals(new BigDecimal("-1025.00"), arr.balanceEntries().get(2).acctCurrBal());
        assertEquals(new BigDecimal("-2500.00"), arr.balanceEntries().get(2).acctCurrCycDebit());
    }

    @Test
    void buildArrayRecord_slots4and5_initializedToZero() {
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        ArrayRecord arr = AccountProcessor.buildArrayRecord(acct);
        assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(3).acctCurrBal());
        assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(3).acctCurrCycDebit());
        assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(4).acctCurrBal());
        assertEquals(BigDecimal.ZERO, arr.balanceEntries().get(4).acctCurrCycDebit());
    }

    @Test
    void buildArrayRecord_has5Entries() {
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        ArrayRecord arr = AccountProcessor.buildArrayRecord(acct);
        assertEquals(5, arr.balanceEntries().size());
    }

    // ---- VbrRecord1 tests (1500-POPUL-VBRC-RECORD part 1) ----

    @Test
    void buildVbrRecord1_copiesIdAndStatus() {
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        VbrRecord1 vbr1 = AccountProcessor.buildVbrRecord1(acct);
        assertEquals("00000000001", vbr1.acctId());
        assertEquals("Y", vbr1.acctActiveStatus());
    }

    // ---- VbrRecord2 tests (1500-POPUL-VBRC-RECORD part 2) ----

    @Test
    void buildVbrRecord2_copiesIdBalanceLimitAndYear() {
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        VbrRecord2 vbr2 = AccountProcessor.buildVbrRecord2(acct);
        assertEquals("00000000001", vbr2.acctId());
        assertEquals(new BigDecimal("194.00"), vbr2.acctCurrBal());
        assertEquals(new BigDecimal("2020.00"), vbr2.acctCreditLimit());
        // Year extracted from WS-ACCT-REISSUE-YYYY (first 4 chars of reissue date)
        assertEquals("2025", vbr2.acctReissueYear());
    }

    @Test
    void buildVbrRecord2_nullReissueDate_emptyYear() {
        AccountRecord acct = new AccountRecord(
                "00000000001", "Y",
                new BigDecimal("194.00"), new BigDecimal("2020.00"),
                new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", null,
                BigDecimal.ZERO, BigDecimal.ZERO, "", "A000000000"
        );
        VbrRecord2 vbr2 = AccountProcessor.buildVbrRecord2(acct);
        assertEquals("", vbr2.acctReissueYear());
    }

    // ---- Display tests (1100-DISPLAY-ACCT-RECORD) ----

    @Test
    void displayRecord_contains12Lines() {
        AccountRecord acct = sampleAccount(BigDecimal.ZERO);
        List<String> lines = AccountProcessor.displayRecord(acct);
        assertEquals(12, lines.size());
        assertTrue(lines.get(0).contains("ACCT-ID"));
        assertTrue(lines.get(0).contains("00000000001"));
        assertTrue(lines.get(11).contains("---"));
    }
}
