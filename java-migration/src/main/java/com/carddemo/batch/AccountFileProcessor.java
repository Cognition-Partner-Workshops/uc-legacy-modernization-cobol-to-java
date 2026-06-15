package com.carddemo.batch;

import com.carddemo.io.AccountFileReader;
import com.carddemo.io.CobolFieldParser;
import com.carddemo.io.DateConverter;
import com.carddemo.model.AccountRecord;
import com.carddemo.model.ArrayRecord;
import com.carddemo.model.ArrayRecord.ArrayEntry;
import com.carddemo.model.OutputAccountRecord;
import com.carddemo.model.VbrcRecord1;
import com.carddemo.model.VbrcRecord2;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java translation of COBOL batch program CBACT01C.
 *
 * <p>Business logic:
 * <ol>
 *   <li>Opens an indexed account file (VSAM KSDS) for sequential reading</li>
 *   <li>For each account record:
 *     <ul>
 *       <li>Writes a flat output record with reformatted reissue date and a
 *           business rule substitution for zero cycle debits (→ 2525.00)</li>
 *       <li>Writes an array record with 5 balance/debit entries (3 populated, 2 zero)</li>
 *       <li>Writes two variable-length records (short: ID+status, long: ID+bal+limit+year)</li>
 *     </ul>
 *   </li>
 *   <li>Closes files and terminates</li>
 * </ol>
 */
public class AccountFileProcessor {

    private static final BigDecimal DEBIT_SUBSTITUTION = new BigDecimal("2525.00");
    private static final BigDecimal ARRAY_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARRAY_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARRAY_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARRAY_DEBIT_3 = new BigDecimal("-2500.00");

    private final Path inputFile;
    private final Path outputFile;
    private final Path arrayFile;
    private final Path vbrcFile;

    private final List<OutputAccountRecord> outputRecords = new ArrayList<>();
    private final List<ArrayRecord> arrayRecords = new ArrayList<>();
    private final List<Object> vbrcRecords = new ArrayList<>();

    public AccountFileProcessor(Path inputFile, Path outputFile, Path arrayFile, Path vbrcFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.arrayFile = arrayFile;
        this.vbrcFile = vbrcFile;
    }

    public void execute() throws IOException {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        try (final AccountFileReader reader = new AccountFileReader(inputFile)) {
            AccountRecord record;
            while ((record = reader.readNext()) != null) {
                displayAccountRecord(record);
                processRecord(record);
            }
        }

        writeOutputFile();
        writeArrayFile();
        writeVbrcFile();

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
    }

    private void displayAccountRecord(AccountRecord record) {
        System.out.println("ACCT-ID                 :" + record.acctId());
        System.out.println("ACCT-ACTIVE-STATUS      :" + record.acctActiveStatus());
        System.out.println("ACCT-CURR-BAL           :" + record.acctCurrBal());
        System.out.println("ACCT-CREDIT-LIMIT       :" + record.acctCreditLimit());
        System.out.println("ACCT-CASH-CREDIT-LIMIT  :" + record.acctCurrCycCredit());
        System.out.println("ACCT-OPEN-DATE          :" + record.acctOpenDate());
        System.out.println("ACCT-EXPIRAION-DATE     :" + record.acctExpirationDate());
        System.out.println("ACCT-REISSUE-DATE       :" + record.acctReissueDate());
        System.out.println("ACCT-CURR-CYC-CREDIT    :" + record.acctCurrCycCredit());
        System.out.println("ACCT-CURR-CYC-DEBIT     :" + record.acctCurrCycDebit());
        System.out.println("ACCT-GROUP-ID           :" + record.acctGroupId());
        System.out.println("-------------------------------------------------");
    }

    private void processRecord(AccountRecord record) {
        final OutputAccountRecord outRec = populateOutputRecord(record);
        outputRecords.add(outRec);

        final ArrayRecord arrRec = populateArrayRecord(record);
        arrayRecords.add(arrRec);

        final VbrcRecord1 vb1 = populateVbrcRecord1(record);
        final VbrcRecord2 vb2 = populateVbrcRecord2(record);
        vbrcRecords.add(vb1);
        vbrcRecords.add(vb2);
    }

    /**
     * Equivalent to 1300-POPUL-ACCT-RECORD.
     * Business rules:
     * - Reissue date is reformatted from YYYY-MM-DD to YYYYMMDD via COBDATFT
     * - If cycle debit is zero, substitute 2525.00
     */
    OutputAccountRecord populateOutputRecord(AccountRecord record) {
        final String reformattedReissueDate = DateConverter.convertYyyyMmDdToCompact(
                record.acctReissueDate());

        BigDecimal cycDebit = record.acctCurrCycDebit();
        if (cycDebit.compareTo(BigDecimal.ZERO) == 0) {
            cycDebit = DEBIT_SUBSTITUTION;
        }

        return new OutputAccountRecord(
                record.acctId(),
                record.acctActiveStatus(),
                record.acctCurrBal(),
                record.acctCreditLimit(),
                record.acctCashCreditLimit(),
                record.acctOpenDate(),
                record.acctExpirationDate(),
                reformattedReissueDate,
                record.acctCurrCycCredit(),
                cycDebit,
                record.acctGroupId()
        );
    }

    /**
     * Equivalent to 1400-POPUL-ARRAY-RECORD.
     * Populates a 5-element array per account:
     * - Entry 1: account balance + 1005.00 debit
     * - Entry 2: account balance + 1525.00 debit
     * - Entry 3: -1025.00 balance + -2500.00 debit
     * - Entries 4-5: zero (from INITIALIZE)
     */
    ArrayRecord populateArrayRecord(AccountRecord record) {
        final ArrayEntry[] entries = new ArrayEntry[ArrayRecord.ENTRY_COUNT];
        entries[0] = new ArrayEntry(record.acctCurrBal(), ARRAY_DEBIT_1);
        entries[1] = new ArrayEntry(record.acctCurrBal(), ARRAY_DEBIT_2);
        entries[2] = new ArrayEntry(ARRAY_BAL_3, ARRAY_DEBIT_3);
        entries[3] = ArrayEntry.ZERO;
        entries[4] = ArrayEntry.ZERO;
        return new ArrayRecord(record.acctId(), entries);
    }

    /**
     * Equivalent to 1500-POPUL-VBRC-RECORD.
     */
    VbrcRecord1 populateVbrcRecord1(AccountRecord record) {
        return new VbrcRecord1(record.acctId(), record.acctActiveStatus());
    }

    VbrcRecord2 populateVbrcRecord2(AccountRecord record) {
        final String reissueYear = DateConverter.extractYear(record.acctReissueDate());
        return new VbrcRecord2(
                record.acctId(),
                record.acctCurrBal(),
                record.acctCreditLimit(),
                reissueYear
        );
    }

    private void writeOutputFile() throws IOException {
        try (final PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile))) {
            for (final OutputAccountRecord rec : outputRecords) {
                writer.print(CobolFieldParser.formatUnsignedNumeric(rec.acctId(), 11));
                writer.print(CobolFieldParser.formatAlphanumeric(rec.acctActiveStatus(), 1));
                writer.print(CobolFieldParser.formatSignedDecimal(rec.acctCurrBal(), 12, 2));
                writer.print(CobolFieldParser.formatSignedDecimal(rec.acctCreditLimit(), 12, 2));
                writer.print(CobolFieldParser.formatSignedDecimal(rec.acctCashCreditLimit(), 12, 2));
                writer.print(CobolFieldParser.formatAlphanumeric(rec.acctOpenDate(), 10));
                writer.print(CobolFieldParser.formatAlphanumeric(rec.acctExpirationDate(), 10));
                writer.print(CobolFieldParser.formatAlphanumeric(rec.acctReissueDate(), 10));
                writer.print(CobolFieldParser.formatSignedDecimal(rec.acctCurrCycCredit(), 12, 2));
                writer.print(formatComp3AsHex(rec.acctCurrCycDebit(), 12, 2));
                writer.print(CobolFieldParser.formatAlphanumeric(rec.acctGroupId(), 10));
                writer.println();
            }
        }
    }

    private void writeArrayFile() throws IOException {
        try (final PrintWriter writer = new PrintWriter(Files.newBufferedWriter(arrayFile))) {
            for (final ArrayRecord rec : arrayRecords) {
                writer.print(CobolFieldParser.formatUnsignedNumeric(rec.acctId(), 11));
                for (final ArrayEntry entry : rec.entries()) {
                    writer.print(CobolFieldParser.formatSignedDecimal(entry.acctCurrBal(), 12, 2));
                    writer.print(formatComp3AsHex(entry.acctCurrCycDebit(), 12, 2));
                }
                writer.print(CobolFieldParser.formatAlphanumeric("", 4));
                writer.println();
            }
        }
    }

    private void writeVbrcFile() throws IOException {
        try (final DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(vbrcFile)))) {
            for (final Object rec : vbrcRecords) {
                if (rec instanceof VbrcRecord1 vb1) {
                    final String data = CobolFieldParser.formatUnsignedNumeric(vb1.acctId(), 11)
                            + CobolFieldParser.formatAlphanumeric(vb1.acctActiveStatus(), 1);
                    dos.writeShort(data.length());
                    dos.writeBytes(data);
                } else if (rec instanceof VbrcRecord2 vb2) {
                    final String data = CobolFieldParser.formatUnsignedNumeric(vb2.acctId(), 11)
                            + CobolFieldParser.formatSignedDecimal(vb2.acctCurrBal(), 12, 2)
                            + CobolFieldParser.formatSignedDecimal(vb2.acctCreditLimit(), 12, 2)
                            + CobolFieldParser.formatAlphanumeric(vb2.acctReissueYear(), 4);
                    dos.writeShort(data.length());
                    dos.writeBytes(data);
                }
            }
        }
    }

    /**
     * Formats a COMP-3 value as a hex string for text-based output files.
     * In production mainframe output, COMP-3 is binary packed-decimal;
     * here we represent it as a hex string for portability and testability.
     */
    private static String formatComp3AsHex(BigDecimal value, int totalDigits, int decimalPlaces) {
        final byte[] packed = CobolFieldParser.formatComp3(value, totalDigits, decimalPlaces);
        final StringBuilder sb = new StringBuilder(packed.length * 2);
        for (final byte b : packed) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }

    public List<OutputAccountRecord> getOutputRecords() {
        return List.copyOf(outputRecords);
    }

    public List<ArrayRecord> getArrayRecords() {
        return List.copyOf(arrayRecords);
    }

    public List<Object> getVbrcRecords() {
        return List.copyOf(vbrcRecords);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: AccountFileProcessor <inputFile> <outputFile> <arrayFile> <vbrcFile>");
            System.exit(1);
        }

        final var processor = new AccountFileProcessor(
                Path.of(args[0]),
                Path.of(args[1]),
                Path.of(args[2]),
                Path.of(args[3])
        );
        processor.execute();
    }
}
