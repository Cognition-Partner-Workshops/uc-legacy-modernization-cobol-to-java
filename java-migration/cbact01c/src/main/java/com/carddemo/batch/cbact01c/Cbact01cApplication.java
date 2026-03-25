package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.io.AccountFileReader;
import com.carddemo.batch.cbact01c.io.AccountFileWriter;
import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.ArrayRecord;
import com.carddemo.batch.cbact01c.model.ArrayRecord.BalanceEntry;
import com.carddemo.batch.cbact01c.model.OutAccountRecord;
import com.carddemo.batch.cbact01c.model.VbrRecord1;
import com.carddemo.batch.cbact01c.model.VbrRecord2;
import com.carddemo.batch.cbact01c.util.DateConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Java 17+ migration of COBOL batch program CBACT01C.
 * <p>
 * Original COBOL function:
 * Reads the account VSAM file (ACCTFILE) sequentially and writes each
 * record into three output files in different formats:
 * <ol>
 *   <li>OUTFILE  -- fixed-format account record with date reformatting</li>
 *   <li>ARRYFILE -- array-format record with 5 balance-entry slots</li>
 *   <li>VBRCFILE -- two variable-length records per account (VB1 + VB2)</li>
 * </ol>
 * <p>
 * Key business rules preserved from COBOL:
 * <ul>
 *   <li>Reissue date is converted from YYYY-MM-DD to YYYYMMDD (via COBDATFT replacement)</li>
 *   <li>If ACCT-CURR-CYC-DEBIT is zero, substitute 2525.00 in the output record</li>
 *   <li>Array record slots 1-2 get the account's current balance; slot 3 is hardcoded
 *       to -1025.00 balance / -2500.00 debit; slots 4-5 are zero-filled</li>
 *   <li>Array record debit slots 1-2 are hardcoded to 1005.00 and 1525.00 respectively</li>
 *   <li>VB1 record: account ID + active status (12 bytes)</li>
 *   <li>VB2 record: account ID + balance + credit limit + reissue year (39 bytes)</li>
 * </ul>
 */
public class Cbact01cApplication {

    private final Path inputFile;
    private final Path outFile;
    private final Path arrayFile;
    private final Path vbrFile;

    public Cbact01cApplication(Path inputFile, Path outFile, Path arrayFile, Path vbrFile) {
        this.inputFile = inputFile;
        this.outFile = outFile;
        this.arrayFile = arrayFile;
        this.vbrFile = vbrFile;
    }

    /**
     * Executes the batch process.
     * Mirrors the COBOL PROCEDURE DIVISION main flow.
     *
     * @return the number of records processed
     * @throws IOException if any file operation fails (equivalent to COBOL ABEND)
     */
    public int execute() throws IOException {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        int recordCount = 0;

        try (var reader = new AccountFileReader(inputFile);
             var writer = new AccountFileWriter(outFile, arrayFile, vbrFile)) {

            Optional<AccountRecord> optRecord;
            while ((optRecord = reader.readNext()).isPresent()) {
                AccountRecord acctRec = optRecord.get();

                // 1100-DISPLAY-ACCT-RECORD
                displayAccountRecord(acctRec);

                // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
                OutAccountRecord outRec = buildOutRecord(acctRec);
                writer.writeOutRecord(outRec);

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                ArrayRecord arrRec = buildArrayRecord(acctRec);
                writer.writeArrayRecord(arrRec);

                // 1500-POPUL-VBRC-RECORD + 1550-WRITE-VB1-RECORD + 1575-WRITE-VB2-RECORD
                VbrRecord1 vb1 = buildVbrRecord1(acctRec);
                VbrRecord2 vb2 = buildVbrRecord2(acctRec);
                writer.writeVbrRecord1(vb1);
                writer.writeVbrRecord2(vb2);

                recordCount++;
            }
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return recordCount;
    }

    /**
     * Builds the fixed-format output record.
     * Mirrors COBOL paragraphs 1300-POPUL-ACCT-RECORD.
     */
    static OutAccountRecord buildOutRecord(AccountRecord acct) {
        // COBDATFT date conversion: YYYY-MM-DD -> YYYYMMDD (padded to 10)
        String reformattedReissueDate = DateConverter.convertYyyyMmDdToCompact(acct.reissueDate());

        // Business rule: if debit is zero, substitute 2525.00
        BigDecimal cycDebit = acct.currCycDebit();
        if (cycDebit.compareTo(BigDecimal.ZERO) == 0) {
            cycDebit = new BigDecimal("2525.00");
        }

        return new OutAccountRecord(
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

    /**
     * Builds the array-format output record.
     * Mirrors COBOL paragraph 1400-POPUL-ARRAY-RECORD.
     */
    static ArrayRecord buildArrayRecord(AccountRecord acct) {
        BalanceEntry[] entries = new BalanceEntry[ArrayRecord.OCCURS_COUNT];

        // Slot 1: current balance + hardcoded 1005.00 debit
        entries[0] = new BalanceEntry(acct.currBal(), new BigDecimal("1005.00"));
        // Slot 2: current balance + hardcoded 1525.00 debit
        entries[1] = new BalanceEntry(acct.currBal(), new BigDecimal("1525.00"));
        // Slot 3: hardcoded -1025.00 balance + -2500.00 debit
        entries[2] = new BalanceEntry(new BigDecimal("-1025.00"), new BigDecimal("-2500.00"));
        // Slots 4-5: zero (INITIALIZE sets these to zero)
        entries[3] = BalanceEntry.ZERO;
        entries[4] = BalanceEntry.ZERO;

        return new ArrayRecord(acct.acctId(), entries);
    }

    /**
     * Builds the VB1 record (account ID + status).
     * Mirrors COBOL paragraph 1500-POPUL-VBRC-RECORD (first part).
     */
    static VbrRecord1 buildVbrRecord1(AccountRecord acct) {
        return new VbrRecord1(acct.acctId(), acct.activeStatus());
    }

    /**
     * Builds the VB2 record (account ID + balance + limit + reissue year).
     * Mirrors COBOL paragraph 1500-POPUL-VBRC-RECORD (second part).
     */
    static VbrRecord2 buildVbrRecord2(AccountRecord acct) {
        String reissueYear = DateConverter.extractYear(acct.reissueDate());
        return new VbrRecord2(
                acct.acctId(),
                acct.currBal(),
                acct.creditLimit(),
                reissueYear
        );
    }

    /**
     * Displays an account record to stdout.
     * Mirrors COBOL paragraph 1100-DISPLAY-ACCT-RECORD.
     */
    private void displayAccountRecord(AccountRecord acct) {
        System.out.println("ACCT-ID                 :" + String.format("%011d", acct.acctId()));
        System.out.println("ACCT-ACTIVE-STATUS      :" + acct.activeStatus());
        System.out.println("ACCT-CURR-BAL           :" + acct.currBal());
        System.out.println("ACCT-CREDIT-LIMIT       :" + acct.creditLimit());
        System.out.println("ACCT-CASH-CREDIT-LIMIT  :" + acct.cashCreditLimit());
        System.out.println("ACCT-OPEN-DATE          :" + acct.openDate());
        System.out.println("ACCT-EXPIRAION-DATE     :" + acct.expirationDate());
        System.out.println("ACCT-REISSUE-DATE       :" + acct.reissueDate());
        System.out.println("ACCT-CURR-CYC-CREDIT    :" + acct.currCycCredit());
        System.out.println("ACCT-CURR-CYC-DEBIT     :" + acct.currCycDebit());
        System.out.println("ACCT-GROUP-ID           :" + acct.groupId());
        System.out.println("-------------------------------------------------");
    }

    /**
     * CLI entry point. Expects 4 arguments: inputFile outFile arrayFile vbrFile
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: Cbact01cApplication <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        var app = new Cbact01cApplication(
                Path.of(args[0]), Path.of(args[1]),
                Path.of(args[2]), Path.of(args[3])
        );

        try {
            int count = app.execute();
            System.out.println("Processed " + count + " account records.");
        } catch (IOException e) {
            // Mirrors COBOL 9999-ABEND-PROGRAM
            System.err.println("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }
    }
}
