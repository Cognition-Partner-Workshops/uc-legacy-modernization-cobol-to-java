package com.carddemo.batch.cbact01c.service;

import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.ArrayAccountRecord;
import com.carddemo.batch.cbact01c.model.ArrayAccountRecord.BalanceDebitPair;
import com.carddemo.batch.cbact01c.model.OutAccountRecord;
import com.carddemo.batch.cbact01c.model.VbrRecord1;
import com.carddemo.batch.cbact01c.model.VbrRecord2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the business-logic transformations from CBACT01C paragraphs
 * 1300-POPUL-ACCT-RECORD, 1400-POPUL-ARRAY-RECORD, and 1500-POPUL-VBRC-RECORD.
 */
public final class AccountProcessor {

    /**
     * Default value substituted for ACCT-CURR-CYC-DEBIT when the original
     * value is zero (see paragraph 1300-POPUL-ACCT-RECORD, lines 236-238).
     */
    static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");

    /** Hard-coded debit for array element 1 (index 0). */
    static final BigDecimal ARR_DEBIT_1 = new BigDecimal("1005.00");

    /** Hard-coded debit for array element 2 (index 1). */
    static final BigDecimal ARR_DEBIT_2 = new BigDecimal("1525.00");

    /** Hard-coded balance for array element 3 (index 2). */
    static final BigDecimal ARR_BAL_3 = new BigDecimal("-1025.00");

    /** Hard-coded debit for array element 3 (index 2). */
    static final BigDecimal ARR_DEBIT_3 = new BigDecimal("-2500.00");

    private AccountProcessor() {
        // utility class
    }

    /**
     * Transforms an {@link AccountRecord} into an {@link OutAccountRecord},
     * replicating the COBOL paragraph 1300-POPUL-ACCT-RECORD.
     *
     * <p>Key transformations:
     * <ul>
     *   <li>Reissue date converted from YYYY-MM-DD to YYYYMMDD via COBDATFT</li>
     *   <li>If cycle debit is zero, it is replaced with 2525.00</li>
     * </ul>
     *
     * @param acct the input account record
     * @return the transformed output account record
     */
    public static OutAccountRecord toOutRecord(AccountRecord acct) {
        // Date conversion: YYYY-MM-DD -> YYYYMMDD (COBDATFT with type=2, outtype=2)
        String convertedReissueDate = DateConverter.convert(
                acct.reissueDate(),
                DateConverter.INPUT_YYYY_MM_DD,
                DateConverter.OUTPUT_YYYYMMDD
        );
        // The COBOL output field OUT-ACCT-REISSUE-DATE is PIC X(10), so the
        // 8-char YYYYMMDD result is right-padded with spaces to 10 characters.
        String reissueDatePadded = String.format("%-10s", convertedReissueDate);

        // Business rule: if cycle debit is zero, substitute 2525.00
        BigDecimal cycDebit = acct.currCycDebit().signum() == 0
                ? DEFAULT_CYC_DEBIT
                : acct.currCycDebit();

        return new OutAccountRecord(
                acct.acctId(),
                acct.activeStatus(),
                acct.currBal(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                reissueDatePadded,
                acct.currCycCredit(),
                cycDebit,
                acct.groupId()
        );
    }

    /**
     * Transforms an {@link AccountRecord} into an {@link ArrayAccountRecord},
     * replicating the COBOL paragraph 1400-POPUL-ARRAY-RECORD.
     *
     * <p>The COBOL code INITIALIZEs the array (zeros) then populates:
     * <ul>
     *   <li>Element 1: actual balance, debit 1005.00</li>
     *   <li>Element 2: actual balance, debit 1525.00</li>
     *   <li>Element 3: balance -1025.00, debit -2500.00</li>
     *   <li>Elements 4-5: remain zero</li>
     * </ul>
     *
     * @param acct the input account record
     * @return the array output record
     */
    public static ArrayAccountRecord toArrayRecord(AccountRecord acct) {
        List<BalanceDebitPair> pairs = new ArrayList<>(ArrayAccountRecord.OCCURS_COUNT);

        pairs.add(new BalanceDebitPair(acct.currBal(), ARR_DEBIT_1));
        pairs.add(new BalanceDebitPair(acct.currBal(), ARR_DEBIT_2));
        pairs.add(new BalanceDebitPair(ARR_BAL_3, ARR_DEBIT_3));
        pairs.add(BalanceDebitPair.ZERO);
        pairs.add(BalanceDebitPair.ZERO);

        return new ArrayAccountRecord(acct.acctId(), List.copyOf(pairs));
    }

    /**
     * Builds VBR record 1 from an account, replicating part of
     * COBOL paragraph 1500-POPUL-VBRC-RECORD.
     *
     * @param acct the input account record
     * @return VBR record type 1 (account ID + active status)
     */
    public static VbrRecord1 toVbrRecord1(AccountRecord acct) {
        return new VbrRecord1(acct.acctId(), acct.activeStatus());
    }

    /**
     * Builds VBR record 2 from an account, replicating part of
     * COBOL paragraph 1500-POPUL-VBRC-RECORD.
     *
     * @param acct the input account record
     * @return VBR record type 2 (account ID + balance + credit limit + reissue year)
     */
    public static VbrRecord2 toVbrRecord2(AccountRecord acct) {
        String reissueYear = DateConverter.extractYear(acct.reissueDate());
        return new VbrRecord2(
                acct.acctId(),
                acct.currBal(),
                acct.creditLimit(),
                reissueYear
        );
    }
}
