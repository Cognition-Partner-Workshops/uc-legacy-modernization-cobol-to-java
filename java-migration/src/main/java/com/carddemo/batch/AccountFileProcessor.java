package com.carddemo.batch;

import com.carddemo.model.AccountRecord;
import com.carddemo.model.ArrayRecord;
import com.carddemo.model.OutputAccountRecord;
import com.carddemo.model.VariableLengthRecord;
import com.carddemo.util.DateConverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ port of COBOL batch program <strong>CBACT01C</strong>.
 * <p>
 * Business logic:
 * <ol>
 *   <li>Open four files: one input (account master) and three output files.</li>
 *   <li>Read each account record sequentially from the input file.</li>
 *   <li>For every record:
 *       <ul>
 *         <li>Display (log) the account fields.</li>
 *         <li>Build and write a flat output record (OUTFILE) with date formatting
 *             and a default debit of 2525.00 when the source debit is zero.</li>
 *         <li>Build and write an array-based record (ARRYFILE) with 5 balance/debit
 *             slots — slots 1–2 mirror the account balance with fixed debits;
 *             slot 3 uses hardcoded negative values; slots 4–5 remain zero.</li>
 *         <li>Build and write two variable-length records (VBRCFILE):
 *             a short record (ID + status) and a long record
 *             (ID + balance + credit limit + reissue year).</li>
 *       </ul>
 *   </li>
 *   <li>Close all files.</li>
 * </ol>
 */
public class AccountFileProcessor {

    /** Default debit value when the source cycle debit is zero (COBOL: MOVE 2525.00). */
    private static final BigDecimal DEFAULT_DEBIT = new BigDecimal("2525.00");

    /** Fixed debit for array slot 1. */
    private static final BigDecimal ARRAY_DEBIT_SLOT1 = new BigDecimal("1005.00");
    /** Fixed debit for array slot 2. */
    private static final BigDecimal ARRAY_DEBIT_SLOT2 = new BigDecimal("1525.00");
    /** Fixed balance for array slot 3. */
    private static final BigDecimal ARRAY_BAL_SLOT3 = new BigDecimal("-1025.00");
    /** Fixed debit for array slot 3. */
    private static final BigDecimal ARRAY_DEBIT_SLOT3 = new BigDecimal("-2500.00");

    private final Path inputFile;
    private final Path outFile;
    private final Path arryFile;
    private final Path vbrcFile;

    // Collected records for programmatic access (useful for testing)
    private final List<OutputAccountRecord> outputRecords = new ArrayList<>();
    private final List<ArrayRecord> arrayRecords = new ArrayList<>();
    private final List<VariableLengthRecord> vbrRecords = new ArrayList<>();
    private final List<String> displayLog = new ArrayList<>();

    public AccountFileProcessor(Path inputFile, Path outFile, Path arryFile, Path vbrcFile) {
        this.inputFile = inputFile;
        this.outFile = outFile;
        this.arryFile = arryFile;
        this.vbrcFile = vbrcFile;
    }

    /**
     * Execute the batch process — equivalent to the COBOL PROCEDURE DIVISION.
     *
     * @return the number of account records successfully processed
     * @throws IOException on any file I/O error (mirrors COBOL ABEND)
     */
    public int execute() throws IOException {
        displayLog.add("START OF EXECUTION OF PROGRAM CBACT01C");

        int count = 0;

        try (BufferedReader reader = Files.newBufferedReader(inputFile);
             BufferedWriter outWriter = Files.newBufferedWriter(outFile);
             BufferedWriter arrWriter = Files.newBufferedWriter(arryFile);
             BufferedWriter vbrWriter = Files.newBufferedWriter(vbrcFile)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                AccountRecord acct = AccountRecord.parse(line);

                // 1100-DISPLAY-ACCT-RECORD
                displayAccountRecord(acct);

                // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
                OutputAccountRecord outRec = buildOutputRecord(acct);
                outputRecords.add(outRec);
                outWriter.write(outRec.toOutputLine());
                outWriter.newLine();

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                ArrayRecord arrRec = buildArrayRecord(acct);
                arrayRecords.add(arrRec);
                arrWriter.write(arrRec.toOutputLine());
                arrWriter.newLine();

                // 1500-POPUL-VBRC-RECORD + 1550/1575-WRITE-VBx-RECORD
                VariableLengthRecord.Type1 vb1 = buildVbr1(acct);
                VariableLengthRecord.Type2 vb2 = buildVbr2(acct);
                vbrRecords.add(vb1);
                vbrRecords.add(vb2);
                vbrWriter.write(vb1.toOutputLine());
                vbrWriter.newLine();
                vbrWriter.write(vb2.toOutputLine());
                vbrWriter.newLine();

                count++;
            }
        }

        displayLog.add("END OF EXECUTION OF PROGRAM CBACT01C");
        return count;
    }

    // ── Record builders (mirror COBOL paragraphs) ────────────────────

    /**
     * 1300-POPUL-ACCT-RECORD — build the flat output record.
     * <p>
     * Special rules:
     * <ul>
     *   <li>Reissue date is reformatted via COBDATFT (YYYY-MM-DD → YYYYMMDD).</li>
     *   <li>If cycle debit is zero, substitute 2525.00.</li>
     * </ul>
     */
    OutputAccountRecord buildOutputRecord(AccountRecord acct) {
        String formattedReissueDate = DateConverter.toCompactDate(acct.reissueDate());

        BigDecimal debit = acct.currCycDebit().compareTo(BigDecimal.ZERO) == 0
                ? DEFAULT_DEBIT
                : acct.currCycDebit();

        return new OutputAccountRecord(
                acct.acctId(),
                acct.activeStatus(),
                acct.currBal(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                formattedReissueDate,
                acct.currCycCredit(),
                debit,
                acct.groupId());
    }

    /**
     * 1400-POPUL-ARRAY-RECORD — build the array-based record.
     * <p>
     * COBOL populates only slots 1–3; slots 4–5 stay zeroed from INITIALIZE.
     * <ul>
     *   <li>Slot 1: balance = ACCT-CURR-BAL, debit = 1005.00</li>
     *   <li>Slot 2: balance = ACCT-CURR-BAL, debit = 1525.00</li>
     *   <li>Slot 3: balance = -1025.00,      debit = -2500.00</li>
     *   <li>Slots 4–5: zeroed</li>
     * </ul>
     */
    ArrayRecord buildArrayRecord(AccountRecord acct) {
        ArrayRecord arr = ArrayRecord.initialized(acct.acctId());
        arr.balances()[0] = acct.currBal();
        arr.debits()[0] = ARRAY_DEBIT_SLOT1;
        arr.balances()[1] = acct.currBal();
        arr.debits()[1] = ARRAY_DEBIT_SLOT2;
        arr.balances()[2] = ARRAY_BAL_SLOT3;
        arr.debits()[2] = ARRAY_DEBIT_SLOT3;
        // Slots 3, 4 (indices 3, 4) remain BigDecimal.ZERO
        return arr;
    }

    /**
     * 1500-POPUL-VBRC-RECORD — build the short variable-length record (type 1).
     */
    VariableLengthRecord.Type1 buildVbr1(AccountRecord acct) {
        return new VariableLengthRecord.Type1(acct.acctId(), acct.activeStatus());
    }

    /**
     * 1500-POPUL-VBRC-RECORD — build the long variable-length record (type 2).
     * The reissue year is extracted from the reissue date (first 4 characters).
     */
    VariableLengthRecord.Type2 buildVbr2(AccountRecord acct) {
        String reissueYear = acct.reissueDate().length() >= 4
                ? acct.reissueDate().substring(0, 4)
                : acct.reissueDate();
        return new VariableLengthRecord.Type2(
                acct.acctId(), acct.currBal(), acct.creditLimit(), reissueYear);
    }

    /** 1100-DISPLAY-ACCT-RECORD — log the account fields. */
    private void displayAccountRecord(AccountRecord acct) {
        displayLog.add("ACCT-ID                 :" + String.format("%011d", acct.acctId()));
        displayLog.add("ACCT-ACTIVE-STATUS      :" + acct.activeStatus());
        displayLog.add("ACCT-CURR-BAL           :" + acct.currBal().toPlainString());
        displayLog.add("ACCT-CREDIT-LIMIT       :" + acct.creditLimit().toPlainString());
        displayLog.add("ACCT-CASH-CREDIT-LIMIT  :" + acct.cashCreditLimit().toPlainString());
        displayLog.add("ACCT-OPEN-DATE          :" + acct.openDate());
        displayLog.add("ACCT-EXPIRAION-DATE     :" + acct.expirationDate());
        displayLog.add("ACCT-REISSUE-DATE       :" + acct.reissueDate());
        displayLog.add("ACCT-CURR-CYC-CREDIT    :" + acct.currCycCredit().toPlainString());
        displayLog.add("ACCT-CURR-CYC-DEBIT     :" + acct.currCycDebit().toPlainString());
        displayLog.add("ACCT-GROUP-ID           :" + acct.groupId());
        displayLog.add("-------------------------------------------------");
    }

    // ── Accessors for test verification ──────────────────────────────

    public List<OutputAccountRecord> getOutputRecords() { return List.copyOf(outputRecords); }
    public List<ArrayRecord> getArrayRecords() { return List.copyOf(arrayRecords); }
    public List<VariableLengthRecord> getVbrRecords() { return List.copyOf(vbrRecords); }
    public List<String> getDisplayLog() { return List.copyOf(displayLog); }

    // ── CLI entry point ──────────────────────────────────────────────

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: AccountFileProcessor <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        Path acctFile = Path.of(args[0]);
        Path outFile = Path.of(args[1]);
        Path arryFile = Path.of(args[2]);
        Path vbrcFile = Path.of(args[3]);

        AccountFileProcessor processor = new AccountFileProcessor(acctFile, outFile, arryFile, vbrcFile);
        int count = processor.execute();

        // Mirror COBOL DISPLAY statements
        processor.getDisplayLog().forEach(System.out::println);
        System.out.println("Processed " + count + " account records.");
    }
}
