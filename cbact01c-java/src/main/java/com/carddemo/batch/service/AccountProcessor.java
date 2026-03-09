package com.carddemo.batch.service;

import com.carddemo.batch.model.*;
import com.carddemo.batch.util.DateConverter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Core business logic extracted from COBOL program CBACT01C.
 *
 * <p>This class is deliberately stateless and side-effect free so that every
 * transformation can be unit-tested independently of file I/O.
 *
 * <p>Business rules implemented:
 * <ol>
 *   <li>Map each input {@link AccountRecord} to an {@link OutAccountRecord}
 *       (paragraph 1300-POPUL-ACCT-RECORD)</li>
 *   <li>Convert the reissue date from YYYY-MM-DD to YYYYMMDD via the date
 *       converter (CALL 'COBDATFT')</li>
 *   <li>If the current-cycle debit is zero, default it to 2525.00 in the
 *       output record</li>
 *   <li>Build an {@link ArrayRecord} with hardcoded test values
 *       (paragraph 1400-POPUL-ARRAY-RECORD)</li>
 *   <li>Build a pair of variable-length records ({@link VbRecord1} +
 *       {@link VbRecord2}) per account (paragraph 1500-POPUL-VBRC-RECORD)</li>
 * </ol>
 */
public final class AccountProcessor {

    /** Default debit value when ACCT-CURR-CYC-DEBIT is zero. */
    private static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");

    /** Hardcoded array-entry debit values from the COBOL source. */
    private static final BigDecimal ARR_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARR_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARR_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARR_DEBIT_3 = new BigDecimal("-2500.00");

    // ---------------------------------------------------------------
    // Public transformation methods
    // ---------------------------------------------------------------

    /**
     * Transforms an input account record into the flat output record.
     * Corresponds to COBOL paragraph 1300-POPUL-ACCT-RECORD.
     */
    public OutAccountRecord toOutRecord(AccountRecord in) {
        // Date conversion: YYYY-MM-DD (type 2) -> YYYYMMDD (output type 2)
        String convertedDate = DateConverter.convert(in.reissueDate(), "2", "2");
        // Trim to 10 characters as per PIC X(10)
        String reissueDateOut = convertedDate.length() > 10
                ? convertedDate.substring(0, 10)
                : convertedDate;

        // Business rule: if current cycle debit is zero, default to 2525.00
        BigDecimal cycDebit = in.currCycDebit().signum() == 0
                ? DEFAULT_CYC_DEBIT
                : in.currCycDebit();

        return new OutAccountRecord(
                in.acctId(),
                in.activeStatus(),
                in.currBal(),
                in.creditLimit(),
                in.cashCreditLimit(),
                in.openDate(),
                in.expirationDate(),
                reissueDateOut,
                in.currCycCredit(),
                cycDebit,
                in.groupId()
        );
    }

    /**
     * Builds the array record for an account.
     * Corresponds to COBOL paragraph 1400-POPUL-ARRAY-RECORD.
     *
     * <p>The COBOL source initialises the record to zeros, then fills
     * three of the five OCCURS entries with a mix of actual and
     * hardcoded values. Entries 4 and 5 remain at zero.
     */
    public ArrayRecord toArrayRecord(AccountRecord in) {
        ArrayRecord.ArrayEntry[] entries = new ArrayRecord.ArrayEntry[ArrayRecord.OCCURS_COUNT];

        // Index 1: actual balance, hardcoded debit 1005.00
        entries[0] = new ArrayRecord.ArrayEntry(in.currBal(), ARR_DEBIT_1);
        // Index 2: actual balance, hardcoded debit 1525.00
        entries[1] = new ArrayRecord.ArrayEntry(in.currBal(), ARR_DEBIT_2);
        // Index 3: hardcoded balance -1025.00, hardcoded debit -2500.00
        entries[2] = new ArrayRecord.ArrayEntry(ARR_BAL_3, ARR_DEBIT_3);
        // Indices 4 and 5: zero (from INITIALIZE)
        entries[3] = ArrayRecord.ArrayEntry.ZERO;
        entries[4] = ArrayRecord.ArrayEntry.ZERO;

        return new ArrayRecord(in.acctId(), entries);
    }

    /**
     * Builds the VB1 (short) variable-length record.
     * Part of COBOL paragraph 1500-POPUL-VBRC-RECORD.
     */
    public VbRecord1 toVbRecord1(AccountRecord in) {
        return new VbRecord1(in.acctId(), in.activeStatus());
    }

    /**
     * Builds the VB2 (long) variable-length record.
     * Part of COBOL paragraph 1500-POPUL-VBRC-RECORD.
     *
     * <p>The reissue year is extracted from the YYYY portion of the
     * reissue date (WS-ACCT-REISSUE-YYYY, first 4 chars of the
     * YYYY-MM-DD formatted date).
     */
    public VbRecord2 toVbRecord2(AccountRecord in) {
        // Extract year from YYYY-MM-DD
        String reissueYear = in.reissueDate() != null && in.reissueDate().length() >= 4
                ? in.reissueDate().substring(0, 4)
                : "    ";

        return new VbRecord2(in.acctId(), in.currBal(), in.creditLimit(), reissueYear);
    }

    // ---------------------------------------------------------------
    // Batch convenience: process all records at once
    // ---------------------------------------------------------------

    /**
     * Result container holding all output records produced by processing
     * a list of input account records.
     */
    public record BatchResult(
            List<OutAccountRecord> outRecords,
            List<ArrayRecord> arrayRecords,
            List<VbRecord1> vbRecords1,
            List<VbRecord2> vbRecords2
    ) {}

    /**
     * Processes a batch of account records, returning all output records.
     */
    public BatchResult processBatch(List<AccountRecord> accounts) {
        List<OutAccountRecord> outRecords = new ArrayList<>(accounts.size());
        List<ArrayRecord> arrayRecords = new ArrayList<>(accounts.size());
        List<VbRecord1> vbRecords1 = new ArrayList<>(accounts.size());
        List<VbRecord2> vbRecords2 = new ArrayList<>(accounts.size());

        for (AccountRecord acct : accounts) {
            outRecords.add(toOutRecord(acct));
            arrayRecords.add(toArrayRecord(acct));
            vbRecords1.add(toVbRecord1(acct));
            vbRecords2.add(toVbRecord2(acct));
        }

        return new BatchResult(outRecords, arrayRecords, vbRecords1, vbRecords2);
    }
}
