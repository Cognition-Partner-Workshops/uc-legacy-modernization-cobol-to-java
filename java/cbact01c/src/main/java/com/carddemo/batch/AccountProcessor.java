package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileWriter;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.ArrayRecord.BalanceEntry;
import com.carddemo.batch.model.OutAccountRecord;
import com.carddemo.batch.model.VbrcRecord1;
import com.carddemo.batch.model.VbrcRecord2;
import com.carddemo.batch.util.DateFormatter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Core business logic of CBACT01C — processes one {@link AccountRecord}
 * and writes transformed output to the three output files.
 *
 * <p>This class encapsulates the logic from COBOL paragraphs:
 * <ul>
 *   <li>1100-DISPLAY-ACCT-RECORD — display / logging</li>
 *   <li>1300-POPUL-ACCT-RECORD — populate the flat output record</li>
 *   <li>1400-POPUL-ARRAY-RECORD — populate the array output record</li>
 *   <li>1500-POPUL-VBRC-RECORD — populate the two variable-length records</li>
 * </ul>
 */
public final class AccountProcessor {

    /** Default debit value substituted when ACCT-CURR-CYC-DEBIT is zero. */
    private static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");

    /** Fixed debit for array entry index 1 (0-based). */
    private static final BigDecimal ARR_DEBIT_1 = new BigDecimal("1005.00");
    /** Fixed debit for array entry index 2 (0-based). */
    private static final BigDecimal ARR_DEBIT_2 = new BigDecimal("1525.00");
    /** Fixed balance for array entry index 3 (0-based). */
    private static final BigDecimal ARR_BAL_3 = new BigDecimal("-1025.00");
    /** Fixed debit for array entry index 3 (0-based). */
    private static final BigDecimal ARR_DEBIT_3 = new BigDecimal("-2500.00");

    private final AccountFileWriter writer;
    private final List<String> displayLog = new ArrayList<>();

    public AccountProcessor(AccountFileWriter writer) {
        this.writer = writer;
    }

    /**
     * Processes a single account record (corresponds to the main
     * loop body in CBACT01C PROCEDURE DIVISION).
     */
    public void process(AccountRecord acct) throws IOException {
        displayAccountRecord(acct);

        OutAccountRecord outRec = populateOutRecord(acct);
        writer.writeOutRecord(outRec);

        ArrayRecord arrRec = populateArrayRecord(acct);
        writer.writeArrayRecord(arrRec);

        VbrcRecord1 vb1 = populateVbrcRecord1(acct);
        VbrcRecord2 vb2 = populateVbrcRecord2(acct);
        writer.writeVbrcRecord1(vb1);
        writer.writeVbrcRecord2(vb2);
    }

    /**
     * Returns all display log messages captured during processing.
     */
    public List<String> getDisplayLog() {
        return List.copyOf(displayLog);
    }

    // ---------------------------------------------------------------
    // 1100-DISPLAY-ACCT-RECORD
    // ---------------------------------------------------------------
    void displayAccountRecord(AccountRecord acct) {
        displayLog.add("ACCT-ID                 :" + String.format("%011d", acct.acctId()));
        displayLog.add("ACCT-ACTIVE-STATUS      :" + acct.acctActiveStatus());
        displayLog.add("ACCT-CURR-BAL           :" + acct.acctCurrBal());
        displayLog.add("ACCT-CREDIT-LIMIT       :" + acct.acctCreditLimit());
        displayLog.add("ACCT-CASH-CREDIT-LIMIT  :" + acct.acctCashCreditLimit());
        displayLog.add("ACCT-OPEN-DATE          :" + acct.acctOpenDate());
        displayLog.add("ACCT-EXPIRAION-DATE     :" + acct.acctExpiraionDate());
        displayLog.add("ACCT-REISSUE-DATE       :" + acct.acctReissueDate());
        displayLog.add("ACCT-CURR-CYC-CREDIT    :" + acct.acctCurrCycCredit());
        displayLog.add("ACCT-CURR-CYC-DEBIT     :" + acct.acctCurrCycDebit());
        displayLog.add("ACCT-GROUP-ID           :" + acct.acctGroupId());
        displayLog.add("-------------------------------------------------");
    }

    // ---------------------------------------------------------------
    // 1300-POPUL-ACCT-RECORD
    // Populates the flat output record. Key business rules:
    //   - Reissue date is converted from YYYY-MM-DD to YYYYMMDD via COBDATFT
    //   - If ACCT-CURR-CYC-DEBIT == 0, substitute 2525.00
    // ---------------------------------------------------------------
    static OutAccountRecord populateOutRecord(AccountRecord acct) {
        // Date conversion: YYYY-MM-DD -> YYYYMMDD (replicates COBDATFT call)
        String reissueDateFormatted = DateFormatter.convertYyyyMmDdToCompact(acct.acctReissueDate());

        // Pad to 10 chars to match PIC X(10) — YYYYMMDD + 2 spaces
        if (reissueDateFormatted != null && reissueDateFormatted.length() < 10) {
            reissueDateFormatted = reissueDateFormatted
                    + " ".repeat(10 - reissueDateFormatted.length());
        }

        // Business rule: if cycle debit is zero, use default 2525.00
        BigDecimal cycDebit = acct.acctCurrCycDebit();
        if (cycDebit.compareTo(BigDecimal.ZERO) == 0) {
            cycDebit = DEFAULT_CYC_DEBIT;
        }

        return new OutAccountRecord(
                acct.acctId(),
                acct.acctActiveStatus(),
                acct.acctCurrBal(),
                acct.acctCreditLimit(),
                acct.acctCashCreditLimit(),
                acct.acctOpenDate(),
                acct.acctExpiraionDate(),
                reissueDateFormatted,
                acct.acctCurrCycCredit(),
                cycDebit,
                acct.acctGroupId()
        );
    }

    // ---------------------------------------------------------------
    // 1400-POPUL-ARRAY-RECORD
    // Populates array record with 5 entries (only 1-3 are set):
    //   [0]: currBal=ACCT-CURR-BAL, debit=1005.00
    //   [1]: currBal=ACCT-CURR-BAL, debit=1525.00
    //   [2]: currBal=-1025.00,      debit=-2500.00
    //   [3]: zeroed (from INITIALIZE)
    //   [4]: zeroed (from INITIALIZE)
    // ---------------------------------------------------------------
    static ArrayRecord populateArrayRecord(AccountRecord acct) {
        List<BalanceEntry> entries = new ArrayList<>(5);
        entries.add(new BalanceEntry(acct.acctCurrBal(), ARR_DEBIT_1));
        entries.add(new BalanceEntry(acct.acctCurrBal(), ARR_DEBIT_2));
        entries.add(new BalanceEntry(ARR_BAL_3, ARR_DEBIT_3));
        entries.add(BalanceEntry.ZERO);
        entries.add(BalanceEntry.ZERO);

        return new ArrayRecord(acct.acctId(), entries, "    ");
    }

    // ---------------------------------------------------------------
    // 1500-POPUL-VBRC-RECORD
    // Populates two variable-length records:
    //   VB1: acctId + activeStatus
    //   VB2: acctId + currBal + creditLimit + reissueYyyy
    // ---------------------------------------------------------------
    static VbrcRecord1 populateVbrcRecord1(AccountRecord acct) {
        return new VbrcRecord1(acct.acctId(), acct.acctActiveStatus());
    }

    static VbrcRecord2 populateVbrcRecord2(AccountRecord acct) {
        // Extract YYYY from the reissue date (YYYY-MM-DD)
        String reissueYyyy = acct.acctReissueDate() != null && acct.acctReissueDate().length() >= 4
                ? acct.acctReissueDate().substring(0, 4)
                : "    ";
        return new VbrcRecord2(acct.acctId(), acct.acctCurrBal(), acct.acctCreditLimit(), reissueYyyy);
    }
}
