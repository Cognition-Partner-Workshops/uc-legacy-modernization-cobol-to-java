package com.carddemo.batch.service;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord;
import com.carddemo.batch.model.BalanceEntry;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VbrcRecord1;
import com.carddemo.batch.model.VbrcRecord2;

import java.math.BigDecimal;
import java.util.List;

/**
 * Encapsulates the business logic from COBOL paragraphs 1300, 1400, and 1500
 * that transform an input {@link AccountRecord} into the three output formats.
 * <p>
 * Business rules preserved from the original COBOL:
 * <ul>
 *   <li>The reissue date is converted from YYYY-MM-DD to YYYYMMDD
 *       (via COBDATFT with type 2 → type 2)</li>
 *   <li>If the current cycle debit is zero, it is replaced with 2525.00</li>
 *   <li>Array record elements 1-2 use actual balance with hardcoded debits
 *       (1005.00, 1525.00); element 3 uses hardcoded values
 *       (-1025.00 balance, -2500.00 debit); elements 4-5 are zeros</li>
 *   <li>VBR record 1 contains just the account ID and active status</li>
 *   <li>VBR record 2 contains the account ID, current balance, credit limit,
 *       and the 4-digit reissue year</li>
 * </ul>
 */
public final class AccountProcessor {

    /** Default cycle debit value when original is zero (COBOL: MOVE 2525.00) */
    public static final BigDecimal DEFAULT_CYCLE_DEBIT = new BigDecimal("2525.00");

    /** Hardcoded debit for array element 1 */
    public static final BigDecimal ARRAY_DEBIT_1 = new BigDecimal("1005.00");

    /** Hardcoded debit for array element 2 */
    public static final BigDecimal ARRAY_DEBIT_2 = new BigDecimal("1525.00");

    /** Hardcoded balance for array element 3 */
    public static final BigDecimal ARRAY_BALANCE_3 = new BigDecimal("-1025.00");

    /** Hardcoded debit for array element 3 */
    public static final BigDecimal ARRAY_DEBIT_3 = new BigDecimal("-2500.00");

    private AccountProcessor() {
        // utility class
    }

    /**
     * Builds the output account record (paragraph 1300-POPUL-ACCT-RECORD).
     * <p>
     * Applies the date conversion on the reissue date and the zero-debit
     * substitution rule.
     *
     * @param input the source account record
     * @return the transformed output record
     */
    public static OutputAccountRecord buildOutputRecord(AccountRecord input) {
        // Convert reissue date: YYYY-MM-DD → YYYYMMDD
        // COBOL: MOVE '2' TO CODATECN-TYPE, MOVE '2' TO CODATECN-OUTTYPE
        String convertedReissueDate = DateConverter.convert(
                input.reissueDate(),
                DateConverter.TYPE_DASHED,
                DateConverter.TYPE_COMPACT
        );

        // If cycle debit is zero, substitute 2525.00
        BigDecimal cycleDebit = input.currentCycleDebit();
        if (cycleDebit.compareTo(BigDecimal.ZERO) == 0) {
            cycleDebit = DEFAULT_CYCLE_DEBIT;
        }

        return new OutputAccountRecord(
                input.acctId(),
                input.activeStatus(),
                input.currentBalance(),
                input.creditLimit(),
                input.cashCreditLimit(),
                input.openDate(),
                input.expirationDate(),
                convertedReissueDate,
                input.currentCycleCredit(),
                cycleDebit,
                input.groupId()
        );
    }

    /**
     * Builds the array account record (paragraph 1400-POPUL-ARRAY-RECORD).
     * <p>
     * Populates 5 balance entries:
     * <ol>
     *   <li>Actual balance, debit = 1005.00</li>
     *   <li>Actual balance, debit = 1525.00</li>
     *   <li>Balance = -1025.00, debit = -2500.00</li>
     *   <li>Zero (from INITIALIZE)</li>
     *   <li>Zero (from INITIALIZE)</li>
     * </ol>
     *
     * @param input the source account record
     * @return the array output record
     */
    public static ArrayAccountRecord buildArrayRecord(AccountRecord input) {
        List<BalanceEntry> entries = List.of(
                new BalanceEntry(input.currentBalance(), ARRAY_DEBIT_1),
                new BalanceEntry(input.currentBalance(), ARRAY_DEBIT_2),
                new BalanceEntry(ARRAY_BALANCE_3, ARRAY_DEBIT_3),
                BalanceEntry.ZERO,
                BalanceEntry.ZERO
        );
        return new ArrayAccountRecord(input.acctId(), entries);
    }

    /**
     * Builds the first variable-length record (paragraph 1500-POPUL-VBRC-RECORD, VB1).
     *
     * @param input the source account record
     * @return VBR record type 1
     */
    public static VbrcRecord1 buildVbrcRecord1(AccountRecord input) {
        return new VbrcRecord1(input.acctId(), input.activeStatus());
    }

    /**
     * Builds the second variable-length record (paragraph 1500-POPUL-VBRC-RECORD, VB2).
     * <p>
     * Extracts the 4-digit year from the reissue date (YYYY-MM-DD → YYYY).
     *
     * @param input the source account record
     * @return VBR record type 2
     */
    public static VbrcRecord2 buildVbrcRecord2(AccountRecord input) {
        // COBOL: MOVE WS-ACCT-REISSUE-YYYY TO VB2-ACCT-REISSUE-YYYY
        // WS-ACCT-REISSUE-YYYY is the first 4 chars of the reissue date
        String reissueYear = input.reissueDate().substring(0, 4);

        return new VbrcRecord2(
                input.acctId(),
                input.currentBalance(),
                input.creditLimit(),
                reissueYear
        );
    }
}
