package com.carddemo.batch.service;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VariableLengthRecord1;
import com.carddemo.batch.model.VariableLengthRecord2;

import java.math.BigDecimal;

/**
 * Core business-logic of the CBACT01C batch program.
 *
 * <p>For every input {@link AccountRecord} read from the VSAM account master,
 * this processor produces three sets of output records that mirror the COBOL
 * paragraphs 1300, 1400 and 1500:
 * <ol>
 *   <li>{@link OutputAccountRecord} — flat account extract with date conversion
 *       and debit-zero substitution</li>
 *   <li>{@link ArrayRecord} — array-style record with 5 balance/debit slots</li>
 *   <li>{@link VariableLengthRecord1} + {@link VariableLengthRecord2} — two
 *       variable-length records per account</li>
 * </ol>
 */
public final class AccountFileProcessor {

    /** Default debit value substituted when the input debit is zero (paragraph 1300). */
    private static final BigDecimal DEFAULT_DEBIT = new BigDecimal("2525.00");

    /** Hardcoded debit for array slot 1 (paragraph 1400). */
    private static final BigDecimal ARRAY_DEBIT_SLOT1 = new BigDecimal("1005.00");

    /** Hardcoded debit for array slot 2 (paragraph 1400). */
    private static final BigDecimal ARRAY_DEBIT_SLOT2 = new BigDecimal("1525.00");

    /** Hardcoded balance for array slot 3 (paragraph 1400). */
    private static final BigDecimal ARRAY_BAL_SLOT3 = new BigDecimal("-1025.00");

    /** Hardcoded debit for array slot 3 (paragraph 1400). */
    private static final BigDecimal ARRAY_DEBIT_SLOT3 = new BigDecimal("-2500.00");

    private AccountFileProcessor() {
        // utility class
    }

    // ── 1300-POPUL-ACCT-RECORD ──────────────────────────────────────────

    /**
     * Build the output account record from the input account record.
     * Mirrors COBOL paragraph 1300-POPUL-ACCT-RECORD.
     *
     * <p>Business rules:
     * <ul>
     *   <li>The reissue date is converted from YYYY-MM-DD (type 2) to
     *       YYYYMMDD (type 2 output) via the COBDATFT equivalent.</li>
     *   <li>If the input current-cycle debit is zero, the output is set
     *       to 2525.00.</li>
     * </ul>
     */
    public static OutputAccountRecord buildOutputRecord(AccountRecord input) {
        // Date conversion: YYYY-MM-DD → YYYYMMDD (type '2' in, type '2' out)
        String convertedReissueDate = DateConverter.convert(
                input.acctReissueDate(),
                DateConverter.INPUT_TYPE_HYPHENATED,
                DateConverter.OUTPUT_TYPE_COMPACT
        );

        // Debit substitution: if zero, replace with 2525.00
        BigDecimal cycDebit = input.acctCurrCycDebit();
        if (cycDebit.compareTo(BigDecimal.ZERO) == 0) {
            cycDebit = DEFAULT_DEBIT;
        }

        return new OutputAccountRecord(
                input.acctId(),
                input.acctActiveStatus(),
                input.acctCurrBal(),
                input.acctCreditLimit(),
                input.acctCashCreditLimit(),
                input.acctOpenDate(),
                input.acctExpiraionDate(),
                convertedReissueDate,
                input.acctCurrCycCredit(),
                cycDebit,
                input.acctGroupId()
        );
    }

    // ── 1400-POPUL-ARRAY-RECORD ─────────────────────────────────────────

    /**
     * Build the array record from the input account record.
     * Mirrors COBOL paragraph 1400-POPUL-ARRAY-RECORD.
     *
     * <p>Business rules:
     * <ul>
     *   <li>Slots 1 and 2 balance = input current balance</li>
     *   <li>Slot 1 debit = 1005.00, Slot 2 debit = 1525.00</li>
     *   <li>Slot 3 balance = -1025.00, Slot 3 debit = -2500.00</li>
     *   <li>Slots 4 and 5 remain zero-initialised</li>
     * </ul>
     */
    public static ArrayRecord buildArrayRecord(AccountRecord input) {
        ArrayRecord record = ArrayRecord.initialise(input.acctId());

        BigDecimal[] balances = record.balances();
        BigDecimal[] debits = record.cycleDebits();

        // Slot 1 (index 0)
        balances[0] = input.acctCurrBal();
        debits[0] = ARRAY_DEBIT_SLOT1;

        // Slot 2 (index 1)
        balances[1] = input.acctCurrBal();
        debits[1] = ARRAY_DEBIT_SLOT2;

        // Slot 3 (index 2)
        balances[2] = ARRAY_BAL_SLOT3;
        debits[2] = ARRAY_DEBIT_SLOT3;

        // Slots 4 and 5 (indices 3, 4) stay at zero from initialise()

        return new ArrayRecord(input.acctId(), balances, debits);
    }

    // ── 1500-POPUL-VBRC-RECORD ──────────────────────────────────────────

    /**
     * Build the short variable-length record (VB1) from the input.
     * Mirrors the VB1 portion of COBOL paragraph 1500-POPUL-VBRC-RECORD.
     */
    public static VariableLengthRecord1 buildVbRecord1(AccountRecord input) {
        return new VariableLengthRecord1(
                input.acctId(),
                input.acctActiveStatus()
        );
    }

    /**
     * Build the long variable-length record (VB2) from the input.
     * Mirrors the VB2 portion of COBOL paragraph 1500-POPUL-VBRC-RECORD.
     *
     * <p>The reissue year is extracted from the YYYY-MM-DD reissue date
     * (equivalent to {@code WS-ACCT-REISSUE-YYYY}).
     */
    public static VariableLengthRecord2 buildVbRecord2(AccountRecord input) {
        String reissueYear = DateConverter.extractYear(input.acctReissueDate());

        return new VariableLengthRecord2(
                input.acctId(),
                input.acctCurrBal(),
                input.acctCreditLimit(),
                reissueYear
        );
    }
}
