package com.carddemo.batch;

import com.carddemo.batch.io.FileProcessingException;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VbrcRecord1;
import com.carddemo.batch.model.VbrcRecord2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ modernization of the COBOL batch program <strong>CBACT01C</strong>.
 *
 * <h2>Original COBOL program summary</h2>
 * <ul>
 *   <li>Reads account records sequentially from an indexed VSAM KSDS file
 *       (DD name ACCTFILE)</li>
 *   <li>For every record it writes three outputs:
 *       <ol>
 *         <li><strong>OUTFILE</strong> — a flat sequential file with selected
 *             account fields; the reissue date is reformatted (YYYY-MM-DD →
 *             YYYYMMDD) and a zero cycle-debit is defaulted to 2525.00</li>
 *         <li><strong>ARRYFILE</strong> — an array-based record with 5 slots
 *             of (balance, cycle-debit); only slots 1-3 are populated</li>
 *         <li><strong>VBRCFILE</strong> — two variable-length records per
 *             account: a short status record (12 bytes) and a longer
 *             financial record (39 bytes)</li>
 *       </ol>
 *   </li>
 *   <li>Displays each account record's fields to SYSOUT</li>
 *   <li>Abends (via CEE3ABD) on any file I/O error</li>
 * </ul>
 */
public class AccountFileProcessor {

    private final Path inputFile;
    private final Path outFile;
    private final Path arrayFile;
    private final Path vbrcFile;
    private final PrintStream displayOut;

    /**
     * Create a processor with explicit file paths.
     *
     * @param inputFile  path to the account input data file
     * @param outFile    path for the output flat file
     * @param arrayFile  path for the array output file
     * @param vbrcFile   path for the variable-length record output file
     * @param displayOut stream for DISPLAY output (typically System.out)
     */
    public AccountFileProcessor(Path inputFile, Path outFile,
                                Path arrayFile, Path vbrcFile,
                                PrintStream displayOut) {
        this.inputFile = inputFile;
        this.outFile = outFile;
        this.arrayFile = arrayFile;
        this.vbrcFile = vbrcFile;
        this.displayOut = displayOut;
    }

    /**
     * Execute the batch job. This is the Java equivalent of the
     * COBOL PROCEDURE DIVISION main logic.
     *
     * @return the list of account records that were processed (useful for testing)
     */
    public List<AccountRecord> execute() {
        displayOut.println("START OF EXECUTION OF PROGRAM CBACT01C");

        List<AccountRecord> processed = new ArrayList<>();

        try (BufferedReader reader = openInput();
             BufferedWriter outWriter = openOutput(outFile, "OUTFILE");
             BufferedWriter arrWriter = openOutput(arrayFile, "ARRYFILE");
             BufferedWriter vbrWriter = openOutput(vbrcFile, "VBRCFILE")) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                AccountRecord acct = AccountRecord.parse(line);
                processed.add(acct);

                // 1100-DISPLAY-ACCT-RECORD
                displayAccountRecord(acct);

                // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
                OutputAccountRecord outRec = OutputAccountRecord.fromAccountRecord(acct);
                writeLine(outWriter, outRec.toFixedWidth(), "OUTFILE");

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                ArrayRecord arrRec = ArrayRecord.fromAccountRecord(acct);
                writeLine(arrWriter, arrRec.toFixedWidth(), "ARRYFILE");

                // 1500-POPUL-VBRC-RECORD
                VbrcRecord1 vb1 = VbrcRecord1.fromAccountRecord(acct);
                VbrcRecord2 vb2 = VbrcRecord2.fromAccountRecord(acct);
                displayOut.println("VBRC-REC1:" + vb1.toFixedWidth());
                displayOut.println("VBRC-REC2:" + vb2.toFixedWidth());

                // 1550-WRITE-VB1-RECORD + 1575-WRITE-VB2-RECORD
                writeLine(vbrWriter, vb1.toFixedWidth(), "VBRCFILE");
                writeLine(vbrWriter, vb2.toFixedWidth(), "VBRCFILE");
            }

        } catch (IOException e) {
            throw new FileProcessingException("I/O error during batch processing", e);
        }

        displayOut.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return processed;
    }

    // ------------------------------------------------------------------
    // File I/O helpers — correspond to COBOL 0000/2000/3000/4000/9000
    // ------------------------------------------------------------------

    private BufferedReader openInput() {
        try {
            return Files.newBufferedReader(inputFile);
        } catch (IOException e) {
            displayOut.println("ERROR OPENING ACCTFILE");
            throw new FileProcessingException("Error opening account input file", e);
        }
    }

    private BufferedWriter openOutput(Path path, String ddName) {
        try {
            return Files.newBufferedWriter(path);
        } catch (IOException e) {
            displayOut.println("ERROR OPENING " + ddName);
            throw new FileProcessingException("Error opening " + ddName, e);
        }
    }

    private void writeLine(BufferedWriter writer, String data, String ddName) {
        try {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) {
            displayOut.println(ddName + " WRITE STATUS IS: ERROR");
            throw new FileProcessingException("Error writing to " + ddName, e);
        }
    }

    // ------------------------------------------------------------------
    // Display — corresponds to COBOL 1100-DISPLAY-ACCT-RECORD
    // ------------------------------------------------------------------

    private void displayAccountRecord(AccountRecord acct) {
        displayOut.println("ACCT-ID                 :" + String.format("%011d", acct.acctId()));
        displayOut.println("ACCT-ACTIVE-STATUS      :" + acct.activeStatus());
        displayOut.println("ACCT-CURR-BAL           :" + acct.currBal());
        displayOut.println("ACCT-CREDIT-LIMIT       :" + acct.creditLimit());
        displayOut.println("ACCT-CASH-CREDIT-LIMIT  :" + acct.cashCreditLimit());
        displayOut.println("ACCT-OPEN-DATE          :" + acct.openDate());
        displayOut.println("ACCT-EXPIRAION-DATE     :" + acct.expirationDate());
        displayOut.println("ACCT-REISSUE-DATE       :" + acct.reissueDate());
        displayOut.println("ACCT-CURR-CYC-CREDIT    :" + acct.currCycCredit());
        displayOut.println("ACCT-CURR-CYC-DEBIT     :" + acct.currCycDebit());
        displayOut.println("ACCT-GROUP-ID           :" + acct.groupId());
        displayOut.println("-------------------------------------------------");
    }

    // ------------------------------------------------------------------
    // CLI entry point
    // ------------------------------------------------------------------

    /**
     * Run the processor from the command line.
     * <p>
     * Usage: {@code java -jar cbact01c.jar <acctfile> <outfile> <arryfile> <vbrcfile>}
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: cbact01c <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        AccountFileProcessor processor = new AccountFileProcessor(
                Path.of(args[0]),
                Path.of(args[1]),
                Path.of(args[2]),
                Path.of(args[3]),
                System.out
        );

        try {
            processor.execute();
        } catch (FileProcessingException e) {
            System.err.println("ABENDING PROGRAM");
            System.err.println(e.getMessage());
            System.exit(999);
        }
    }
}
