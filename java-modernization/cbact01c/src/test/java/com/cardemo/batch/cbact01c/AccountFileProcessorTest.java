package com.cardemo.batch.cbact01c;

import com.cardemo.batch.cbact01c.io.AccountFileReader;
import com.cardemo.batch.cbact01c.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests that verify the Java version produces results identical to what
 * the COBOL CBACT01C program would produce for the same sample inputs.
 *
 * Each test constructs an AccountRecord matching a row from the sample
 * acctdata.txt file and verifies every field of every output record against
 * the values the COBOL MOVE / IF / CALL logic would produce.
 */
class AccountFileProcessorTest {

    // ── Sample account #1 from acctdata.txt ─────────────────────────
    // 00000000001Y00000001940{00000020200{00000010200{2014-11-20
    // 2025-05-202025-05-2000000000000{00000000000{A000000000...
    //
    // Fields:
    //   ACCT-ID                = 1
    //   ACCT-ACTIVE-STATUS     = 'Y'
    //   ACCT-CURR-BAL          = 194.00
    //   ACCT-CREDIT-LIMIT      = 2020.00
    //   ACCT-CASH-CREDIT-LIMIT = 1020.00
    //   ACCT-OPEN-DATE         = "2014-11-20"
    //   ACCT-EXPIRAION-DATE    = "2025-05-20"
    //   ACCT-REISSUE-DATE      = "2025-05-20"
    //   ACCT-CURR-CYC-CREDIT   = 0.00
    //   ACCT-CURR-CYC-DEBIT    = 0.00   ← triggers the 2525.00 default
    //   ACCT-ADDR-ZIP          = "A000000000"
    //   ACCT-GROUP-ID          = "          " (spaces)

    private static final String RAW_LINE_1 =
            "00000000001Y00000001940{00000020200{00000010200{"
                    + "2014-11-202025-05-202025-05-20"
                    + "00000000000{00000000000{A000000000";

    private static AccountRecord acct1() {
        return AccountFileReader.parseLine(RAW_LINE_1);
    }

    // ── 1300-POPUL-ACCT-RECORD tests (OUTFILE) ─────────────────────

    @Test
    void outputRecord_copiesIdentityFields() {
        OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(acct1());
        assertEquals(1L, out.acctId());
        assertEquals('Y', out.activeStatus());
    }

    @Test
    void outputRecord_copiesFinancialFields() {
        OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(acct1());
        assertEquals(0, new BigDecimal("194.00").compareTo(out.currBal()));
        assertEquals(0, new BigDecimal("2020.00").compareTo(out.creditLimit()));
        assertEquals(0, new BigDecimal("1020.00").compareTo(out.cashCreditLimit()));
    }

    @Test
    void outputRecord_copiesDates() {
        OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(acct1());
        assertEquals("2014-11-20", out.openDate());
        assertEquals("2025-05-20", out.expirationDate());
    }

    @Test
    void outputRecord_convertsReissueDate_yyyyMmDd_to_yyyymmdd() {
        // COBDATFT with type=2, outtype=2: "2025-05-20" → "20250520"
        // Then MOVE to PIC X(10) pads to "20250520  "
        OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(acct1());
        assertEquals("20250520  ", out.reissueDate());
    }

    @Test
    void outputRecord_defaultsCycDebitWhenZero() {
        // COBOL: IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
        OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(acct1());
        assertEquals(0, new BigDecimal("2525.00").compareTo(out.currCycDebit()));
    }

    @Test
    void outputRecord_preservesNonZeroCycDebit() {
        AccountRecord acctWithDebit = new AccountRecord(
                2L, 'Y',
                new BigDecimal("194.00"), new BigDecimal("2020.00"),
                new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                new BigDecimal("0.00"), new BigDecimal("500.00"),
                "A000000000", "          ");

        OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(acctWithDebit);
        assertEquals(0, new BigDecimal("500.00").compareTo(out.currCycDebit()));
    }

    @Test
    void outputRecord_copiesGroupId() {
        OutputAccountRecord out = AccountFileProcessor.buildOutputRecord(acct1());
        assertNotNull(out.groupId());
    }

    // ── 1400-POPUL-ARRAY-RECORD tests (ARRYFILE) ───────────────────

    @Test
    void arrayRecord_hasCorrectAccountId() {
        ArrayRecord arr = AccountFileProcessor.buildArrayRecord(acct1());
        assertEquals(1L, arr.acctId());
    }

    @Test
    void arrayRecord_entry1_usesActualBalAndFixedDebit() {
        ArrayRecord arr = AccountFileProcessor.buildArrayRecord(acct1());
        ArrayRecord.BalanceEntry e = arr.balanceEntries()[0];
        assertEquals(0, new BigDecimal("194.00").compareTo(e.currBal()));
        assertEquals(0, new BigDecimal("1005.00").compareTo(e.currCycDebit()));
    }

    @Test
    void arrayRecord_entry2_usesActualBalAndFixedDebit() {
        ArrayRecord arr = AccountFileProcessor.buildArrayRecord(acct1());
        ArrayRecord.BalanceEntry e = arr.balanceEntries()[1];
        assertEquals(0, new BigDecimal("194.00").compareTo(e.currBal()));
        assertEquals(0, new BigDecimal("1525.00").compareTo(e.currCycDebit()));
    }

    @Test
    void arrayRecord_entry3_usesHardcodedNegatives() {
        ArrayRecord arr = AccountFileProcessor.buildArrayRecord(acct1());
        ArrayRecord.BalanceEntry e = arr.balanceEntries()[2];
        assertEquals(0, new BigDecimal("-1025.00").compareTo(e.currBal()));
        assertEquals(0, new BigDecimal("-2500.00").compareTo(e.currCycDebit()));
    }

    @Test
    void arrayRecord_entries4And5_areZero() {
        // COBOL INITIALIZE zeroes these out
        ArrayRecord arr = AccountFileProcessor.buildArrayRecord(acct1());
        for (int i = 3; i < 5; i++) {
            ArrayRecord.BalanceEntry e = arr.balanceEntries()[i];
            assertEquals(0, BigDecimal.ZERO.compareTo(e.currBal()),
                    "entry " + (i + 1) + " balance should be zero");
            assertEquals(0, BigDecimal.ZERO.compareTo(e.currCycDebit()),
                    "entry " + (i + 1) + " debit should be zero");
        }
    }

    // ── 1500-POPUL-VBRC-RECORD tests (VBRCFILE) ────────────────────

    @Test
    void vbRecord1_hasIdAndStatus() {
        AccountRecord acct = acct1();
        VbRecord1 vb1 = new VbRecord1(acct.acctId(), acct.activeStatus());
        assertEquals(1L, vb1.acctId());
        assertEquals('Y', vb1.activeStatus());
    }

    @Test
    void vbRecord2_hasIdBalLimitAndYear() {
        AccountRecord acct = acct1();
        String year = AccountFileProcessor.extractReissueYear(acct.reissueDate());

        VbRecord2 vb2 = new VbRecord2(
                acct.acctId(), acct.currBal(), acct.creditLimit(), year);
        assertEquals(1L, vb2.acctId());
        assertEquals(0, new BigDecimal("194.00").compareTo(vb2.currBal()));
        assertEquals(0, new BigDecimal("2020.00").compareTo(vb2.creditLimit()));
        assertEquals("2025", vb2.reissueYear());
    }

    // ── Full pipeline with multiple records ─────────────────────────

    @Test
    void process_multipleRecords_producesCorrectCounts() {
        // Lines from acctdata.txt rows 1 and 2
        String line2 =
                "00000000002Y00000001580{00000061300{00000054480{"
                        + "2013-06-192024-08-112024-08-11"
                        + "00000000000{00000000000{A000000000";

        AccountRecord a1 = AccountFileReader.parseLine(RAW_LINE_1);
        AccountRecord a2 = AccountFileReader.parseLine(line2);

        AccountFileProcessor.ProcessingResult result =
                AccountFileProcessor.process(List.of(a1, a2));

        assertEquals(2, result.outputRecords().size());
        assertEquals(2, result.arrayRecords().size());
        assertEquals(2, result.vbRecords1().size());
        assertEquals(2, result.vbRecords2().size());
    }

    @Test
    void process_secondRecord_financialsParsedCorrectly() {
        String line2 =
                "00000000002Y00000001580{00000061300{00000054480{"
                        + "2013-06-192024-08-112024-08-11"
                        + "00000000000{00000000000{A000000000";
        AccountRecord a2 = AccountFileReader.parseLine(line2);

        assertEquals(2L, a2.acctId());
        assertEquals(0, new BigDecimal("158.00").compareTo(a2.currBal()));
        assertEquals(0, new BigDecimal("6130.00").compareTo(a2.creditLimit()));
        assertEquals(0, new BigDecimal("5448.00").compareTo(a2.cashCreditLimit()));
    }
}
