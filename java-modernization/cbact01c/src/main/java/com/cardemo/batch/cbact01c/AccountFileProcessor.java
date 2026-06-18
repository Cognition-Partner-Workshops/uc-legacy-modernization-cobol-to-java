package com.cardemo.batch.cbact01c;

import com.cardemo.batch.cbact01c.io.AccountFileReader;
import com.cardemo.batch.cbact01c.io.RecordFormatter;
import com.cardemo.batch.cbact01c.model.*;
import com.cardemo.batch.cbact01c.util.DateConverter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Java equivalent of the COBOL batch program CBACT01C.
 *
 * <p>Reads an account master file sequentially, transforms each record,
 * and writes three output files:
 * <ol>
 *   <li><b>OUTFILE</b> – flat account extract with formatted dates and
 *       a defaulted cycle-debit of 2525.00 when the source value is zero.</li>
 *   <li><b>ARRYFILE</b> – array-structured records with 5 balance/debit
 *       pairs (indices 1-3 populated, 4-5 zeroed).</li>
 *   <li><b>VBRCFILE</b> – two variable-length records per account:
 *       a short summary (id + status) and a long summary (id + bal + limit + year).</li>
 * </ol>
 */
public final class AccountFileProcessor {

    private static final Logger LOG = Logger.getLogger(AccountFileProcessor.class.getName());

    private static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARRAY_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARRAY_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARRAY_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARRAY_DEBIT_3 = new BigDecimal("-2500.00");

    private AccountFileProcessor() {}

    /**
     * Result container holding the records that would be written to each output file.
     */
    public record ProcessingResult(
            List<OutputAccountRecord> outputRecords,
            List<ArrayRecord> arrayRecords,
            List<VbRecord1> vbRecords1,
            List<VbRecord2> vbRecords2
    ) {}

    /**
     * Process account records and return the transformed results.
     */
    public static ProcessingResult process(Iterable<AccountRecord> accounts) {
        List<OutputAccountRecord> outputRecords = new ArrayList<>();
        List<ArrayRecord> arrayRecords = new ArrayList<>();
        List<VbRecord1> vbRecords1 = new ArrayList<>();
        List<VbRecord2> vbRecords2 = new ArrayList<>();

        for (AccountRecord acct : accounts) {
            LOG.fine(() -> "Processing account: " + acct.acctId());

            outputRecords.add(buildOutputRecord(acct));
            arrayRecords.add(buildArrayRecord(acct));

            String reissueYear = extractReissueYear(acct.reissueDate());
            vbRecords1.add(new VbRecord1(acct.acctId(), acct.activeStatus()));
            vbRecords2.add(new VbRecord2(
                    acct.acctId(), acct.currBal(), acct.creditLimit(), reissueYear));
        }

        return new ProcessingResult(outputRecords, arrayRecords, vbRecords1, vbRecords2);
    }

    /**
     * Execute the full batch: read input file, process, write output files.
     */
    public static ProcessingResult execute(
            Path inputFile, Path outFile, Path arryFile, Path vbrcFile)
            throws IOException {

        LOG.info("START OF EXECUTION OF PROGRAM CBACT01C");

        ProcessingResult result;
        try (AccountFileReader reader = new AccountFileReader(inputFile)) {
            result = process(reader);
        }

        writeOutputFile(outFile, result.outputRecords());
        writeArrayFile(arryFile, result.arrayRecords());
        writeVbrcFile(vbrcFile, result.vbRecords1(), result.vbRecords2());

        LOG.info("END OF EXECUTION OF PROGRAM CBACT01C");
        return result;
    }

    // ── 1300-POPUL-ACCT-RECORD ───────────────────────────────────────

    static OutputAccountRecord buildOutputRecord(AccountRecord acct) {
        // Date conversion: YYYY-MM-DD → YYYYMMDD  (type 2 → outtype 2)
        // Then padded to 10 chars (COBOL MOVE truncates/pads X(20) → X(10))
        String convertedDate = DateConverter.convert(acct.reissueDate(), '2', '2');
        String reissueDate = padRight(convertedDate, 10);

        // If cycle debit is zero, substitute 2525.00
        BigDecimal cycDebit = acct.currCycDebit().signum() == 0
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
                reissueDate,
                acct.currCycCredit(),
                cycDebit,
                acct.groupId());
    }

    // ── 1400-POPUL-ARRAY-RECORD ──────────────────────────────────────

    static ArrayRecord buildArrayRecord(AccountRecord acct) {
        ArrayRecord.BalanceEntry[] entries = new ArrayRecord.BalanceEntry[ArrayRecord.NUM_ENTRIES];
        entries[0] = new ArrayRecord.BalanceEntry(acct.currBal(), ARRAY_DEBIT_1);
        entries[1] = new ArrayRecord.BalanceEntry(acct.currBal(), ARRAY_DEBIT_2);
        entries[2] = new ArrayRecord.BalanceEntry(ARRAY_BAL_3, ARRAY_DEBIT_3);
        entries[3] = ArrayRecord.BalanceEntry.ZERO;
        entries[4] = ArrayRecord.BalanceEntry.ZERO;

        return new ArrayRecord(acct.acctId(), entries, "    ");
    }

    // ── helpers ──────────────────────────────────────────────────────

    static String extractReissueYear(String reissueDate) {
        if (reissueDate == null || reissueDate.length() < 4) return "    ";
        return reissueDate.substring(0, 4);
    }

    private static void writeOutputFile(
            Path path, List<OutputAccountRecord> records) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path)) {
            for (OutputAccountRecord r : records) {
                w.write(RecordFormatter.formatOutput(r));
                w.newLine();
            }
        }
    }

    private static void writeArrayFile(
            Path path, List<ArrayRecord> records) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path)) {
            for (ArrayRecord r : records) {
                w.write(RecordFormatter.formatArray(r));
                w.newLine();
            }
        }
    }

    private static void writeVbrcFile(
            Path path,
            List<VbRecord1> vb1Records,
            List<VbRecord2> vb2Records) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path)) {
            for (int i = 0; i < vb1Records.size(); i++) {
                w.write(RecordFormatter.formatVb1(vb1Records.get(i)));
                w.newLine();
                w.write(RecordFormatter.formatVb2(vb2Records.get(i)));
                w.newLine();
            }
        }
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }
}
