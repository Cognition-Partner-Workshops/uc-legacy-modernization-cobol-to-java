package com.carddemo.batch;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord.BalanceEntry;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VbRecord1;
import com.carddemo.batch.model.VbRecord2;
import com.carddemo.batch.util.DateFormatter;
import com.carddemo.batch.util.ZonedDecimalParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ migration of COBOL batch program CBACT01C.
 * <p>
 * Reads the indexed account file (ACCTFILE) sequentially and writes three output files:
 * <ol>
 *   <li><b>OUTFILE</b> – selected/transformed account fields (one record per account)</li>
 *   <li><b>ARRYFILE</b> – array-structured records with 5 balance/debit slots per account</li>
 *   <li><b>VBRCFILE</b> – two variable-length records per account (VB1 + VB2)</li>
 * </ol>
 *
 * @see <a href="../../app/cbl/CBACT01C.cbl">Original COBOL source</a>
 */
public class Cbact01cBatch {

    private static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARRAY_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARRAY_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARRAY_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARRAY_DEBIT_3 = new BigDecimal("-2500.00");

    private final Path acctFilePath;
    private final Path outFilePath;
    private final Path arryFilePath;
    private final Path vbrcFilePath;

    private final List<String> displayOutput = new ArrayList<>();

    public Cbact01cBatch(Path acctFilePath, Path outFilePath, Path arryFilePath, Path vbrcFilePath) {
        this.acctFilePath = acctFilePath;
        this.outFilePath = outFilePath;
        this.arryFilePath = arryFilePath;
        this.vbrcFilePath = vbrcFilePath;
    }

    /**
     * Executes the batch job, mirroring the COBOL PROCEDURE DIVISION.
     *
     * @return the number of records processed
     * @throws IOException if any file I/O operation fails
     */
    public int execute() throws IOException {
        display("START OF EXECUTION OF PROGRAM CBACT01C");

        int recordCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(acctFilePath);
             BufferedWriter outWriter = Files.newBufferedWriter(outFilePath);
             BufferedWriter arryWriter = Files.newBufferedWriter(arryFilePath);
             BufferedWriter vbrcWriter = Files.newBufferedWriter(vbrcFilePath)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                // Pad line to RECORD_LENGTH if shorter (mirrors COBOL fixed-width handling)
                if (line.length() < AccountRecord.RECORD_LENGTH) {
                    line = String.format("%-" + AccountRecord.RECORD_LENGTH + "s", line);
                }

                AccountRecord account = parseAccountRecord(line);
                display(formatAccountDisplay(account));

                OutputAccountRecord outRec = buildOutputRecord(account);
                writeOutputRecord(outWriter, outRec);

                ArrayAccountRecord arrRec = buildArrayRecord(account);
                writeArrayRecord(arryWriter, arrRec);

                VbRecord1 vb1 = buildVbRecord1(account);
                VbRecord2 vb2 = buildVbRecord2(account);
                display("VBRC-REC1:" + formatVbRecord1(vb1));
                display("VBRC-REC2:" + formatVbRecord2(vb2));
                writeVbRecord1(vbrcWriter, vb1);
                writeVbRecord2(vbrcWriter, vb2);

                recordCount++;
            }
        }

        display("END OF EXECUTION OF PROGRAM CBACT01C");
        return recordCount;
    }

    // ---------------------------------------------------------------
    // Parsing (mirrors 1000-ACCTFILE-GET-NEXT / READ INTO ACCOUNT-RECORD)
    // ---------------------------------------------------------------

    /**
     * Parses a 300-byte fixed-width line into an {@link AccountRecord}.
     * Field positions follow CVACT01Y copybook.
     */
    static AccountRecord parseAccountRecord(String line) {
        long acctId = Long.parseLong(line.substring(0, 11).trim());
        String activeStatus = line.substring(11, 12);
        BigDecimal currBal = ZonedDecimalParser.parse(line.substring(12, 24), 2);
        BigDecimal creditLimit = ZonedDecimalParser.parse(line.substring(24, 36), 2);
        BigDecimal cashCreditLimit = ZonedDecimalParser.parse(line.substring(36, 48), 2);
        String openDate = line.substring(48, 58);
        String expirationDate = line.substring(58, 68);
        String reissueDate = line.substring(68, 78);
        BigDecimal currCycCredit = ZonedDecimalParser.parse(line.substring(78, 90), 2);
        BigDecimal currCycDebit = ZonedDecimalParser.parse(line.substring(90, 102), 2);
        String addrZip = line.substring(102, 112).trim();
        String groupId = line.substring(112, 122).trim();

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId
        );
    }

    // ---------------------------------------------------------------
    // Output record building (mirrors 1300-POPUL-ACCT-RECORD)
    // ---------------------------------------------------------------

    OutputAccountRecord buildOutputRecord(AccountRecord acct) {
        // Date conversion: COBDATFT with input type '2' (YYYY-MM-DD) → output type '2' (YYYYMMDD)
        String reformattedReissueDate = DateFormatter.convert(acct.reissueDate(), "2", "2");

        // Business rule: if debit is zero, substitute 2525.00
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
                reformattedReissueDate,
                acct.currCycCredit(),
                cycDebit,
                acct.groupId()
        );
    }

    // ---------------------------------------------------------------
    // Array record building (mirrors 1400-POPUL-ARRAY-RECORD)
    // ---------------------------------------------------------------

    static ArrayAccountRecord buildArrayRecord(AccountRecord acct) {
        List<BalanceEntry> entries = new ArrayList<>(5);
        entries.add(new BalanceEntry(acct.currBal(), ARRAY_DEBIT_1));
        entries.add(new BalanceEntry(acct.currBal(), ARRAY_DEBIT_2));
        entries.add(new BalanceEntry(ARRAY_BAL_3, ARRAY_DEBIT_3));
        entries.add(BalanceEntry.ZERO);
        entries.add(BalanceEntry.ZERO);
        return new ArrayAccountRecord(acct.acctId(), entries);
    }

    // ---------------------------------------------------------------
    // Variable-length record building (mirrors 1500-POPUL-VBRC-RECORD)
    // ---------------------------------------------------------------

    static VbRecord1 buildVbRecord1(AccountRecord acct) {
        return new VbRecord1(acct.acctId(), acct.activeStatus());
    }

    static VbRecord2 buildVbRecord2(AccountRecord acct) {
        // Extract year portion from reissue date (YYYY-MM-DD → first 4 chars)
        String reissueYear = acct.reissueDate() != null && acct.reissueDate().length() >= 4
                ? acct.reissueDate().substring(0, 4)
                : "";
        return new VbRecord2(acct.acctId(), acct.currBal(), acct.creditLimit(), reissueYear);
    }

    // ---------------------------------------------------------------
    // File writing (mirrors 1350/1450/1550/1575 WRITE paragraphs)
    // ---------------------------------------------------------------

    private static void writeOutputRecord(BufferedWriter writer, OutputAccountRecord rec) throws IOException {
        writer.write(String.format("%011d|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s",
                rec.acctId(),
                rec.activeStatus(),
                rec.currBal().toPlainString(),
                rec.creditLimit().toPlainString(),
                rec.cashCreditLimit().toPlainString(),
                rec.openDate(),
                rec.expirationDate(),
                rec.reissueDate(),
                rec.currCycCredit().toPlainString(),
                rec.currCycDebit().toPlainString(),
                rec.groupId()));
        writer.newLine();
    }

    private static void writeArrayRecord(BufferedWriter writer, ArrayAccountRecord rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        for (ArrayAccountRecord.BalanceEntry entry : rec.entries()) {
            sb.append("|").append(entry.currBal().toPlainString());
            sb.append("|").append(entry.currCycDebit().toPlainString());
        }
        writer.write(sb.toString());
        writer.newLine();
    }

    private static void writeVbRecord1(BufferedWriter writer, VbRecord1 rec) throws IOException {
        writer.write(String.format("VB1|%011d|%s", rec.acctId(), rec.activeStatus()));
        writer.newLine();
    }

    private static void writeVbRecord2(BufferedWriter writer, VbRecord2 rec) throws IOException {
        writer.write(String.format("VB2|%011d|%s|%s|%s",
                rec.acctId(),
                rec.currBal().toPlainString(),
                rec.creditLimit().toPlainString(),
                rec.reissueYear()));
        writer.newLine();
    }

    // ---------------------------------------------------------------
    // Display (mirrors COBOL DISPLAY statements)
    // ---------------------------------------------------------------

    private void display(String message) {
        displayOutput.add(message);
        System.out.println(message);
    }

    /** Returns all DISPLAY output produced during execution (for testing). */
    public List<String> getDisplayOutput() {
        return List.copyOf(displayOutput);
    }

    private static String formatAccountDisplay(AccountRecord acct) {
        return String.join(System.lineSeparator(),
                "ACCT-ID                 :" + String.format("%011d", acct.acctId()),
                "ACCT-ACTIVE-STATUS      :" + acct.activeStatus(),
                "ACCT-CURR-BAL           :" + acct.currBal().toPlainString(),
                "ACCT-CREDIT-LIMIT       :" + acct.creditLimit().toPlainString(),
                "ACCT-CASH-CREDIT-LIMIT  :" + acct.cashCreditLimit().toPlainString(),
                "ACCT-OPEN-DATE          :" + acct.openDate(),
                "ACCT-EXPIRAION-DATE     :" + acct.expirationDate(),
                "ACCT-REISSUE-DATE       :" + acct.reissueDate(),
                "ACCT-CURR-CYC-CREDIT    :" + acct.currCycCredit().toPlainString(),
                "ACCT-CURR-CYC-DEBIT     :" + acct.currCycDebit().toPlainString(),
                "ACCT-GROUP-ID           :" + acct.groupId(),
                "-------------------------------------------------");
    }

    private static String formatVbRecord1(VbRecord1 rec) {
        return String.format("%011d%s", rec.acctId(), rec.activeStatus());
    }

    private static String formatVbRecord2(VbRecord2 rec) {
        return String.format("%011d%s%s%s",
                rec.acctId(),
                rec.currBal().toPlainString(),
                rec.creditLimit().toPlainString(),
                rec.reissueYear());
    }

    // ---------------------------------------------------------------
    // Main entry point
    // ---------------------------------------------------------------

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: Cbact01cBatch <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        Cbact01cBatch batch = new Cbact01cBatch(
                Path.of(args[0]), Path.of(args[1]), Path.of(args[2]), Path.of(args[3]));

        try {
            int count = batch.execute();
            System.out.println("Processed " + count + " account records.");
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }
    }
}
