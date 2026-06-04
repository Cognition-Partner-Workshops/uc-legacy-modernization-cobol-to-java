package com.cardemo.batch.service;

import com.cardemo.batch.model.*;
import com.cardemo.batch.model.ArrayRecord.BalanceEntry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Core business logic extracted from CBACT01C.cbl PROCEDURE DIVISION.
 * Transforms each AccountRecord into the three output record types.
 */
public final class AccountFileProcessor {

    private static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARR_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARR_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARR_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARR_DEBIT_3 = new BigDecimal("-2500.00");

    private AccountFileProcessor() {}

    /**
     * Corresponds to 1300-POPUL-ACCT-RECORD.
     * Applies business rules:
     * - Calls date format conversion on reissue date (YYYY-MM-DD -> YYYYMMDD)
     * - Substitutes 2525.00 for currCycDebit when it equals zero
     */
    public static OutputAccountRecord buildOutputRecord(AccountRecord acct) {
        String reformattedReissueDate = DateFormatService.convert(
                acct.reissueDate(), "2", "2");

        BigDecimal cycDebit = acct.currCycDebit().compareTo(BigDecimal.ZERO) == 0
                ? DEFAULT_CYC_DEBIT
                : acct.currCycDebit();

        return new OutputAccountRecord(
                acct.acctId(),
                acct.activeStatus(),
                acct.currBal(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                reformattedReissueDate,
                acct.currCycCredit(),
                cycDebit,
                acct.groupId()
        );
    }

    /**
     * Corresponds to 1400-POPUL-ARRAY-RECORD.
     * Populates 5 balance entries:
     *   [0] = (currBal, 1005.00)
     *   [1] = (currBal, 1525.00)
     *   [2] = (-1025.00, -2500.00)
     *   [3] = (0, 0)  -- initialized but not explicitly populated
     *   [4] = (0, 0)  -- initialized but not explicitly populated
     */
    public static ArrayRecord buildArrayRecord(AccountRecord acct) {
        List<BalanceEntry> entries = new ArrayList<>(ArrayRecord.ARRAY_SIZE);
        entries.add(new BalanceEntry(acct.currBal(), ARR_DEBIT_1));
        entries.add(new BalanceEntry(acct.currBal(), ARR_DEBIT_2));
        entries.add(new BalanceEntry(ARR_BAL_3, ARR_DEBIT_3));
        entries.add(BalanceEntry.ZERO);
        entries.add(BalanceEntry.ZERO);
        return new ArrayRecord(acct.acctId(), entries);
    }

    /**
     * Corresponds to 1500-POPUL-VBRC-RECORD (first record).
     */
    public static VbRecord1 buildVbRecord1(AccountRecord acct) {
        return new VbRecord1(acct.acctId(), acct.activeStatus());
    }

    /**
     * Corresponds to 1500-POPUL-VBRC-RECORD (second record).
     * Uses the year portion of WS-ACCT-REISSUE-DATE.
     */
    public static VbRecord2 buildVbRecord2(AccountRecord acct) {
        String reissueYear = DateFormatService.extractYear(acct.reissueDate());
        return new VbRecord2(
                acct.acctId(),
                acct.currBal(),
                acct.creditLimit(),
                reissueYear
        );
    }
}
