package com.carddemo.batch.service;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.ArrayRecord.BalanceEntry;
import com.carddemo.batch.model.OutAccountRecord;
import com.carddemo.batch.model.VbrRecord1;
import com.carddemo.batch.model.VbrRecord2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the core business logic from the COBOL PROCEDURE DIVISION.
 * Transforms an {@link AccountRecord} into the three output record types.
 */
public final class AccountProcessor {

    private static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARRAY_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARRAY_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARRAY_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARRAY_DEBIT_3 = new BigDecimal("-2500.00");

    private AccountProcessor() {
        // utility class
    }

    /**
     * 1300-POPUL-ACCT-RECORD: Populate the output account record.
     *
     * Business rules:
     * <ul>
     *   <li>Most fields are copied directly from the input record.</li>
     *   <li>The reissue date is reformatted from YYYY-MM-DD to YYYYMMDD
     *       via the COBDATFT date formatter.</li>
     *   <li>If ACCT-CURR-CYC-DEBIT equals zero, it is replaced with 2525.00.</li>
     * </ul>
     */
    public static OutAccountRecord buildOutRecord(AccountRecord acct) {
        String formattedReissueDate = DateFormatter.formatDateCompact(acct.acctReissueDate());

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
                formattedReissueDate,
                acct.acctCurrCycCredit(),
                cycDebit,
                acct.acctGroupId()
        );
    }

    /**
     * 1400-POPUL-ARRAY-RECORD: Populate the array output record.
     *
     * Business rules:
     * <ul>
     *   <li>Slot 1: balance = ACCT-CURR-BAL, debit = 1005.00</li>
     *   <li>Slot 2: balance = ACCT-CURR-BAL, debit = 1525.00</li>
     *   <li>Slot 3: balance = -1025.00,      debit = -2500.00</li>
     *   <li>Slots 4-5: initialized to zero (COBOL INITIALIZE)</li>
     * </ul>
     */
    public static ArrayRecord buildArrayRecord(AccountRecord acct) {
        List<BalanceEntry> entries = new ArrayList<>(5);
        entries.add(new BalanceEntry(acct.acctCurrBal(), ARRAY_DEBIT_1));
        entries.add(new BalanceEntry(acct.acctCurrBal(), ARRAY_DEBIT_2));
        entries.add(new BalanceEntry(ARRAY_BAL_3, ARRAY_DEBIT_3));
        entries.add(new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO));
        entries.add(new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO));
        return new ArrayRecord(acct.acctId(), entries);
    }

    /**
     * 1500-POPUL-VBRC-RECORD: Populate the two variable-length output records.
     *
     * Record 1 (12 bytes): ACCT-ID + ACCT-ACTIVE-STATUS
     */
    public static VbrRecord1 buildVbrRecord1(AccountRecord acct) {
        return new VbrRecord1(acct.acctId(), acct.acctActiveStatus());
    }

    /**
     * Record 2 (39 bytes): ACCT-ID + ACCT-CURR-BAL + ACCT-CREDIT-LIMIT +
     *                       reissue year (first 4 chars of ACCT-REISSUE-DATE).
     */
    public static VbrRecord2 buildVbrRecord2(AccountRecord acct) {
        String reissueYear = "";
        if (acct.acctReissueDate() != null && acct.acctReissueDate().length() >= 4) {
            reissueYear = acct.acctReissueDate().substring(0, 4);
        }
        return new VbrRecord2(
                acct.acctId(),
                acct.acctCurrBal(),
                acct.acctCreditLimit(),
                reissueYear
        );
    }

    /**
     * 1100-DISPLAY-ACCT-RECORD: Generate display lines for a record.
     */
    public static List<String> displayRecord(AccountRecord acct) {
        List<String> lines = new ArrayList<>();
        lines.add("ACCT-ID                 :" + acct.acctId());
        lines.add("ACCT-ACTIVE-STATUS      :" + acct.acctActiveStatus());
        lines.add("ACCT-CURR-BAL           :" + acct.acctCurrBal().toPlainString());
        lines.add("ACCT-CREDIT-LIMIT       :" + acct.acctCreditLimit().toPlainString());
        lines.add("ACCT-CASH-CREDIT-LIMIT  :" + acct.acctCashCreditLimit().toPlainString());
        lines.add("ACCT-OPEN-DATE          :" + acct.acctOpenDate());
        lines.add("ACCT-EXPIRAION-DATE     :" + acct.acctExpiraionDate());
        lines.add("ACCT-REISSUE-DATE       :" + acct.acctReissueDate());
        lines.add("ACCT-CURR-CYC-CREDIT    :" + acct.acctCurrCycCredit().toPlainString());
        lines.add("ACCT-CURR-CYC-DEBIT     :" + acct.acctCurrCycDebit().toPlainString());
        lines.add("ACCT-GROUP-ID           :" + acct.acctGroupId());
        lines.add("-------------------------------------------------");
        return lines;
    }
}
