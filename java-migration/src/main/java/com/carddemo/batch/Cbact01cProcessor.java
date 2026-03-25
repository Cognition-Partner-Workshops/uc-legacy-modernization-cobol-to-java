package com.carddemo.batch;

import com.carddemo.batch.exception.AbendException;
import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.io.DateConverter;
import com.carddemo.batch.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java equivalent of the CBACT01C batch COBOL program.
 *
 * Business logic:
 *   1. Opens an indexed account file (VSAM KSDS) for sequential reading.
 *   2. Opens three output files: OUT-FILE, ARRY-FILE, VBRC-FILE.
 *   3. For each account record read:
 *      a. Displays (logs) all account fields.
 *      b. Populates and writes an OUT-ACCT-REC with selected fields.
 *         - Calls COBDATFT (date converter) to reformat the reissue date.
 *         - If ACCT-CURR-CYC-DEBIT is zero, substitutes 2525.00.
 *      c. Populates and writes an ARR-ARRAY-REC with 5 balance/debit entries
 *         (only entries 1-3 are filled; 4-5 default to zero).
 *      d. Populates and writes two variable-length records (VBRC-REC1 short,
 *         VBRC-REC2 long) carrying a subset of account data.
 *   4. Closes all files. Abends on any I/O error.
 */
public class Cbact01cProcessor {

    private static final Logger log = LoggerFactory.getLogger(Cbact01cProcessor.class);

    /** Default cycle-debit value used when the input value is zero (COBOL: MOVE 2525.00) */
    static final BigDecimal DEFAULT_CYCLE_DEBIT = new BigDecimal("2525.00");

    /** Fixed array debit values populated in 1400-POPUL-ARRAY-RECORD */
    static final BigDecimal ARR_DEBIT_1 = new BigDecimal("1005.00");
    static final BigDecimal ARR_DEBIT_2 = new BigDecimal("1525.00");
    static final BigDecimal ARR_BAL_3 = new BigDecimal("-1025.00");
    static final BigDecimal ARR_DEBIT_3 = new BigDecimal("-2500.00");

    private final Path inputFile;
    private final Path outFile;
    private final Path arrayFile;
    private final Path vbrcFile;

    public Cbact01cProcessor(Path inputFile, Path outFile, Path arrayFile, Path vbrcFile) {
        this.inputFile = inputFile;
        this.outFile = outFile;
        this.arrayFile = arrayFile;
        this.vbrcFile = vbrcFile;
    }

    /**
     * Execute the batch processing.
     *
     * @return the processing result containing all generated records
     */
    public ProcessingResult execute() {
        log.info("START OF EXECUTION OF PROGRAM CBACT01C");

        List<OutAccountRecord> outRecords = new ArrayList<>();
        List<ArrayRecord> arrayRecords = new ArrayList<>();
        List<String> vbrcLines = new ArrayList<>();

        try (AccountFileReader reader = openAccountFile();
             BufferedWriter outWriter = openOutputFile(outFile, "OUTFILE");
             BufferedWriter arrayWriter = openOutputFile(arrayFile, "ARRAYFILE");
             BufferedWriter vbrcWriter = openOutputFile(vbrcFile, "VBRCFILE")) {

            while (reader.hasNext()) {
                AccountRecord account = reader.readNext();
                if (account == null) {
                    break;
                }

                // 1100-DISPLAY-ACCT-RECORD
                displayAccountRecord(account);

                // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
                OutAccountRecord outRecord = populateOutRecord(account);
                outRecords.add(outRecord);
                writeRecord(outWriter, outRecord.toDelimitedLine(), "OUTFILE");

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                ArrayRecord arrayRecord = populateArrayRecord(account);
                arrayRecords.add(arrayRecord);
                writeRecord(arrayWriter, arrayRecord.toDelimitedLine(), "ARRAYFILE");

                // 1500-POPUL-VBRC-RECORD + 1550/1575-WRITE-VB1/VB2-RECORD
                VbrcRecord1 vb1 = populateVbrcRecord1(account);
                VbrcRecord2 vb2 = populateVbrcRecord2(account);

                log.debug("VBRC-REC1:{}", vb1.toDelimitedLine());
                log.debug("VBRC-REC2:{}", vb2.toDelimitedLine());

                String vb1Line = vb1.toDelimitedLine();
                String vb2Line = vb2.toDelimitedLine();
                vbrcLines.add(vb1Line);
                vbrcLines.add(vb2Line);
                writeRecord(vbrcWriter, vb1Line, "VBRCFILE");
                writeRecord(vbrcWriter, vb2Line, "VBRCFILE");
            }

        } catch (IOException e) {
            log.error("I/O error during processing", e);
            throw new AbendException(999, "I/O error during processing", e);
        }

        log.info("END OF EXECUTION OF PROGRAM CBACT01C");
        return new ProcessingResult(outRecords, arrayRecords, vbrcLines);
    }

    // ---------------------------------------------------------------
    // 1100-DISPLAY-ACCT-RECORD
    // ---------------------------------------------------------------
    private void displayAccountRecord(AccountRecord acct) {
        log.info("ACCT-ID                 :{}", acct.formattedAcctId());
        log.info("ACCT-ACTIVE-STATUS      :{}", acct.activeStatus());
        log.info("ACCT-CURR-BAL           :{}", acct.currBal());
        log.info("ACCT-CREDIT-LIMIT       :{}", acct.creditLimit());
        log.info("ACCT-CASH-CREDIT-LIMIT  :{}", acct.cashCreditLimit());
        log.info("ACCT-OPEN-DATE          :{}", acct.openDate());
        log.info("ACCT-EXPIRAION-DATE     :{}", acct.expirationDate());
        log.info("ACCT-REISSUE-DATE       :{}", acct.reissueDate());
        log.info("ACCT-CURR-CYC-CREDIT    :{}", acct.currCycCredit());
        log.info("ACCT-CURR-CYC-DEBIT     :{}", acct.currCycDebit());
        log.info("ACCT-GROUP-ID           :{}", acct.groupId());
        log.info("-------------------------------------------------");
    }

    // ---------------------------------------------------------------
    // 1300-POPUL-ACCT-RECORD
    // ---------------------------------------------------------------
    OutAccountRecord populateOutRecord(AccountRecord acct) {
        // Date conversion: COBDATFT with type=2 (YYYY-MM-DD input), outtype=2 (YYYYMMDD output)
        String reissueDate = DateConverter.convert(
                acct.reissueDate(),
                DateConverter.INPUT_YYYY_MM_DD,
                DateConverter.OUTPUT_YYYYMMDD
        );

        // If ACCT-CURR-CYC-DEBIT equals zero, substitute 2525.00
        BigDecimal currCycDebit = acct.currCycDebit().compareTo(BigDecimal.ZERO) == 0
                ? DEFAULT_CYCLE_DEBIT
                : acct.currCycDebit();

        return new OutAccountRecord(
                acct.acctId(),
                acct.activeStatus(),
                acct.currBal(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                reissueDate,
                acct.currCycCredit(),
                currCycDebit,
                acct.groupId()
        );
    }

    // ---------------------------------------------------------------
    // 1400-POPUL-ARRAY-RECORD
    // ---------------------------------------------------------------
    ArrayRecord populateArrayRecord(AccountRecord acct) {
        // Entries 1-3 are populated; entries 4-5 default to zero (COBOL INITIALIZE)
        return ArrayRecord.of(acct.acctId(),
                new ArrayRecord.BalanceEntry(acct.currBal(), ARR_DEBIT_1),   // Entry 1
                new ArrayRecord.BalanceEntry(acct.currBal(), ARR_DEBIT_2),   // Entry 2
                new ArrayRecord.BalanceEntry(ARR_BAL_3, ARR_DEBIT_3)         // Entry 3
        );
    }

    // ---------------------------------------------------------------
    // 1500-POPUL-VBRC-RECORD
    // ---------------------------------------------------------------
    VbrcRecord1 populateVbrcRecord1(AccountRecord acct) {
        return new VbrcRecord1(acct.acctId(), acct.activeStatus());
    }

    VbrcRecord2 populateVbrcRecord2(AccountRecord acct) {
        // Extract year from reissue date (YYYY-MM-DD → first 4 chars)
        String reissueYear = acct.reissueDate().substring(0, 4);
        return new VbrcRecord2(acct.acctId(), acct.currBal(), acct.creditLimit(), reissueYear);
    }

    // ---------------------------------------------------------------
    // File I/O helpers
    // ---------------------------------------------------------------
    private AccountFileReader openAccountFile() {
        try {
            return new AccountFileReader(inputFile);
        } catch (IOException e) {
            log.error("ERROR OPENING ACCTFILE");
            displayIoStatus(e);
            throw new AbendException(999, "Error opening account file: " + inputFile, e);
        }
    }

    private BufferedWriter openOutputFile(Path path, String name) {
        try {
            return Files.newBufferedWriter(path);
        } catch (IOException e) {
            log.error("ERROR OPENING {}: {}", name, e.getMessage());
            displayIoStatus(e);
            throw new AbendException(999, "Error opening " + name + ": " + path, e);
        }
    }

    private void writeRecord(BufferedWriter writer, String line, String fileName) {
        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            log.error("{} WRITE STATUS IS: ERROR", fileName);
            displayIoStatus(e);
            throw new AbendException(999, "Error writing to " + fileName, e);
        }
    }

    private void displayIoStatus(IOException e) {
        log.error("FILE STATUS IS: {}", e.getMessage());
    }

    /**
     * Holds the complete results of a processing run for testing purposes.
     */
    public record ProcessingResult(
            List<OutAccountRecord> outRecords,
            List<ArrayRecord> arrayRecords,
            List<String> vbrcLines
    ) { }
}
