package com.carddemo.batch;

import com.carddemo.io.AccountFileReader;
import com.carddemo.io.OutputFileWriter;
import com.carddemo.model.AccountRecord;
import com.carddemo.model.ArrayRecord;
import com.carddemo.model.ArrayRecord.BalanceEntry;
import com.carddemo.model.OutputAccountRecord;
import com.carddemo.model.VariableLengthRecord;
import com.carddemo.util.DateFormatter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ migration of COBOL batch program CBACT01C.
 *
 * CBACT01C reads the account master VSAM file sequentially and produces
 * three output files:
 *   1. OUT-FILE:  Transformed account records (with date reformatting
 *                 and conditional debit substitution)
 *   2. ARRY-FILE: Array records with 5 balance/debit pairs per account
 *   3. VBRC-FILE: Two variable-length records per account (VB1 + VB2)
 *
 * Business rules preserved from COBOL:
 *   - Reissue date is reformatted from YYYY-MM-DD to YYYYMMDD via
 *     COBDATFT (now {@link DateFormatter#toCompact})
 *   - If cycle debit is zero, substitute 2525.00 in the output record
 *   - Array entries [1] and [2] get the account's current balance with
 *     hardcoded debit values; entry [3] gets hardcoded negative values;
 *     entries [4] and [5] remain zero-initialized
 *   - VB1 carries account ID + active status
 *   - VB2 carries account ID + balance + credit limit + reissue year
 */
public class AccountFileProcessor {

    /** Hardcoded debit substitution when input debit is zero. */
    private static final BigDecimal DEBIT_SUBSTITUTION = new BigDecimal("2525.00");

    /** Hardcoded values for array record population (per COBOL 1400-POPUL-ARRAY-RECORD). */
    private static final BigDecimal ARRAY_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARRAY_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARRAY_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARRAY_DEBIT_3 = new BigDecimal("-2500.00");

    private final Path inputFile;
    private final Path outFile;
    private final Path arrayFile;
    private final Path vbrcFile;

    public AccountFileProcessor(Path inputFile, Path outFile, Path arrayFile, Path vbrcFile) {
        this.inputFile = inputFile;
        this.outFile = outFile;
        this.arrayFile = arrayFile;
        this.vbrcFile = vbrcFile;
    }

    /**
     * Process all account records and write the three output files.
     *
     * @return the list of processed account records (for testing/verification)
     */
    public List<AccountRecord> process() throws IOException {
        List<AccountRecord> processed = new ArrayList<>();

        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        try (var reader = new AccountFileReader(inputFile);
             var writer = new OutputFileWriter(outFile, arrayFile, vbrcFile)) {

            for (AccountRecord account : reader) {
                displayAccountRecord(account);

                OutputAccountRecord outputRec = buildOutputRecord(account);
                writer.writeOutputRecord(outputRec);

                ArrayRecord arrayRec = buildArrayRecord(account);
                writer.writeArrayRecord(arrayRec);

                VariableLengthRecord.Type1 vb1 = buildVb1Record(account);
                VariableLengthRecord.Type2 vb2 = buildVb2Record(account);
                writer.writeVariableLengthRecord(vb1);
                writer.writeVariableLengthRecord(vb2);

                processed.add(account);
            }
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return processed;
    }

    /**
     * Build the output account record (mirrors 1300-POPUL-ACCT-RECORD).
     *
     * Key business rules:
     *   - Reissue date reformatted from YYYY-MM-DD to YYYYMMDD
     *   - If cycle debit is zero, substitute 2525.00
     */
    OutputAccountRecord buildOutputRecord(AccountRecord acct) {
        String reformattedReissueDate = DateFormatter.toCompact(acct.reissueDate());

        BigDecimal cycleDebit = acct.currentCycleDebit();
        if (cycleDebit.compareTo(BigDecimal.ZERO) == 0) {
            cycleDebit = DEBIT_SUBSTITUTION;
        }

        return new OutputAccountRecord(
                acct.acctId(),
                acct.activeStatus(),
                acct.currentBalance(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                reformattedReissueDate,
                acct.currentCycleCredit(),
                cycleDebit,
                acct.groupId()
        );
    }

    /**
     * Build the array record (mirrors 1400-POPUL-ARRAY-RECORD).
     *
     * Populates a 5-element array where:
     *   [0] balance = account balance, debit = 1005.00
     *   [1] balance = account balance, debit = 1525.00
     *   [2] balance = -1025.00,        debit = -2500.00
     *   [3] zero (initialized)
     *   [4] zero (initialized)
     */
    ArrayRecord buildArrayRecord(AccountRecord acct) {
        BalanceEntry[] entries = new BalanceEntry[5];
        entries[0] = new BalanceEntry(acct.currentBalance(), ARRAY_DEBIT_1);
        entries[1] = new BalanceEntry(acct.currentBalance(), ARRAY_DEBIT_2);
        entries[2] = new BalanceEntry(ARRAY_BAL_3, ARRAY_DEBIT_3);
        entries[3] = BalanceEntry.zero();
        entries[4] = BalanceEntry.zero();
        return new ArrayRecord(acct.acctId(), entries);
    }

    /**
     * Build VB1 record (mirrors 1500-POPUL-VBRC-RECORD, first part).
     */
    VariableLengthRecord.Type1 buildVb1Record(AccountRecord acct) {
        return new VariableLengthRecord.Type1(acct.acctId(), acct.activeStatus());
    }

    /**
     * Build VB2 record (mirrors 1500-POPUL-VBRC-RECORD, second part).
     *
     * The reissue year is extracted from the reissue date's YYYY portion.
     */
    VariableLengthRecord.Type2 buildVb2Record(AccountRecord acct) {
        String reissueYear = DateFormatter.extractYear(acct.reissueDate());
        return new VariableLengthRecord.Type2(
                acct.acctId(),
                acct.currentBalance(),
                acct.creditLimit(),
                reissueYear
        );
    }

    /**
     * Display account record fields (mirrors 1100-DISPLAY-ACCT-RECORD).
     */
    private void displayAccountRecord(AccountRecord acct) {
        System.out.printf("ACCT-ID                 :%011d%n", acct.acctId());
        System.out.printf("ACCT-ACTIVE-STATUS      :%s%n", acct.activeStatus());
        System.out.printf("ACCT-CURR-BAL           :%s%n", acct.currentBalance().toPlainString());
        System.out.printf("ACCT-CREDIT-LIMIT       :%s%n", acct.creditLimit().toPlainString());
        System.out.printf("ACCT-CASH-CREDIT-LIMIT  :%s%n", acct.cashCreditLimit().toPlainString());
        System.out.printf("ACCT-OPEN-DATE          :%s%n", acct.openDate());
        System.out.printf("ACCT-EXPIRAION-DATE     :%s%n", acct.expirationDate());
        System.out.printf("ACCT-REISSUE-DATE       :%s%n", acct.reissueDate());
        System.out.printf("ACCT-CURR-CYC-CREDIT    :%s%n", acct.currentCycleCredit().toPlainString());
        System.out.printf("ACCT-CURR-CYC-DEBIT     :%s%n", acct.currentCycleDebit().toPlainString());
        System.out.printf("ACCT-GROUP-ID           :%s%n", acct.groupId());
        System.out.println("-------------------------------------------------");
    }

    /**
     * CLI entry point matching the COBOL PROCEDURE DIVISION.
     *
     * Usage: java -jar cbact01c-migration.jar <input> <outfile> <arrayfile> <vbrcfile>
     *
     * If no arguments are provided, uses default paths relative to the
     * repository root (app/data/ASCII/acctdata.txt for input).
     */
    public static void main(String[] args) throws IOException {
        Path input;
        Path out;
        Path array;
        Path vbrc;

        if (args.length >= 4) {
            input = Path.of(args[0]);
            out = Path.of(args[1]);
            array = Path.of(args[2]);
            vbrc = Path.of(args[3]);
        } else {
            Path base = Path.of(".");
            input = base.resolve("app/data/ASCII/acctdata.txt");
            out = base.resolve("output/outfile.dat");
            array = base.resolve("output/arrayfile.dat");
            vbrc = base.resolve("output/vbrcfile.dat");
            out.getParent().toFile().mkdirs();
        }

        var processor = new AccountFileProcessor(input, out, array, vbrc);
        List<AccountRecord> records = processor.process();
        System.out.printf("Processed %d account records.%n", records.size());
    }
}
