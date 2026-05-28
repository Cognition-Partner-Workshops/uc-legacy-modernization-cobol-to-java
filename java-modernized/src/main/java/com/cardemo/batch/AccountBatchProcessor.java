package com.cardemo.batch;

import com.cardemo.batch.io.AccountFileReader;
import com.cardemo.batch.io.CsvFileWriter;
import com.cardemo.batch.model.AccountRecord;
import com.cardemo.batch.model.ArrayRecord;
import com.cardemo.batch.model.ArrayRecord.BalanceEntry;
import com.cardemo.batch.model.OutputAccountRecord;
import com.cardemo.batch.model.VbRecord1;
import com.cardemo.batch.model.VbRecord2;
import com.cardemo.batch.util.DateConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ modernization of COBOL batch program CBACT01C.
 *
 * <p>CBACT01C reads an indexed VSAM account file sequentially and writes
 * three output files:
 * <ol>
 *   <li><b>OUT-FILE</b>: Selected account fields with date reformatting</li>
 *   <li><b>ARRY-FILE</b>: Array-style records (5 balance slots per account)</li>
 *   <li><b>VBRC-FILE</b>: Two variable-length records per account</li>
 * </ol>
 *
 * <p>This processor reproduces identical business logic, including the
 * hardcoded default debit of 2525.00 when the cycle debit is zero, and the
 * fixed array-slot population pattern.
 */
public class AccountBatchProcessor {

    private static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARRAY_DEBIT_SLOT1 = new BigDecimal("1005.00");
    private static final BigDecimal ARRAY_DEBIT_SLOT2 = new BigDecimal("1525.00");
    private static final BigDecimal ARRAY_BAL_SLOT3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARRAY_DEBIT_SLOT3 = new BigDecimal("-2500.00");

    private final Path inputFile;
    private final Path outFile;
    private final Path arrayFile;
    private final Path vbFile;

    public AccountBatchProcessor(Path inputFile, Path outFile, Path arrayFile, Path vbFile) {
        this.inputFile = inputFile;
        this.outFile = outFile;
        this.arrayFile = arrayFile;
        this.vbFile = vbFile;
    }

    public int process() throws IOException {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");
        int recordCount = 0;

        try (AccountFileReader reader = new AccountFileReader(inputFile);
             CsvFileWriter outWriter = new CsvFileWriter(outFile, OutputAccountRecord.csvHeader());
             CsvFileWriter arrWriter = new CsvFileWriter(arrayFile, ArrayRecord.csvHeader());
             CsvFileWriter vbWriter = new CsvFileWriter(vbFile, "RECORD_TYPE," + VbRecord2.csvHeader())) {

            AccountRecord account;
            while ((account = reader.readNext()) != null) {
                displayAccountRecord(account);

                OutputAccountRecord outRec = populateOutputRecord(account);
                outWriter.writeLine(outRec.toCsv());

                ArrayRecord arrRec = populateArrayRecord(account);
                arrWriter.writeLine(arrRec.toCsv());

                VbRecord1 vb1 = populateVbRecord1(account);
                VbRecord2 vb2 = populateVbRecord2(account);
                vbWriter.writeLine("VB1," + vb1.acctId() + "," + vb1.activeStatus() + ",,");
                vbWriter.writeLine("VB2," + vb2.toCsv());

                recordCount++;
            }
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return recordCount;
    }

    /**
     * Corresponds to COBOL paragraph 1100-DISPLAY-ACCT-RECORD.
     */
    void displayAccountRecord(AccountRecord acct) {
        System.out.println("ACCT-ID                 :" + acct.acctId());
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
     * Corresponds to COBOL paragraph 1300-POPUL-ACCT-RECORD.
     * Applies the date conversion (COBDATFT replacement) and the
     * default-debit business rule.
     */
    OutputAccountRecord populateOutputRecord(AccountRecord acct) {
        String reissueDate = DateConverter.isoToCompact(acct.reissueDate());

        BigDecimal cycDebit = acct.currCycDebit().compareTo(BigDecimal.ZERO) == 0
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
                acct.groupId()
        );
    }

    /**
     * Corresponds to COBOL paragraph 1400-POPUL-ARRAY-RECORD.
     * Populates 5 balance slots with a mix of actual and hardcoded values.
     * Slots 4 and 5 remain zero (from INITIALIZE).
     */
    ArrayRecord populateArrayRecord(AccountRecord acct) {
        List<BalanceEntry> entries = new ArrayList<>(ArrayRecord.NUM_BALANCE_SLOTS);

        entries.add(new BalanceEntry(acct.currBal(), ARRAY_DEBIT_SLOT1));
        entries.add(new BalanceEntry(acct.currBal(), ARRAY_DEBIT_SLOT2));
        entries.add(new BalanceEntry(ARRAY_BAL_SLOT3, ARRAY_DEBIT_SLOT3));
        entries.add(new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO));
        entries.add(new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO));

        return new ArrayRecord(acct.acctId(), entries);
    }

    /**
     * Corresponds to COBOL paragraph 1500-POPUL-VBRC-RECORD (VB1 portion).
     */
    VbRecord1 populateVbRecord1(AccountRecord acct) {
        return new VbRecord1(acct.acctId(), acct.activeStatus());
    }

    /**
     * Corresponds to COBOL paragraph 1500-POPUL-VBRC-RECORD (VB2 portion).
     * Extracts the 4-character year from the reissue date.
     */
    VbRecord2 populateVbRecord2(AccountRecord acct) {
        String reissueYear = extractYear(acct.reissueDate());
        return new VbRecord2(
                acct.acctId(),
                acct.currBal(),
                acct.creditLimit(),
                reissueYear
        );
    }

    private String extractYear(String dateStr) {
        if (dateStr != null && dateStr.length() >= 4) {
            return dateStr.substring(0, 4);
        }
        return "0000";
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: AccountBatchProcessor <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        AccountBatchProcessor processor = new AccountBatchProcessor(
                Path.of(args[0]), Path.of(args[1]), Path.of(args[2]), Path.of(args[3])
        );

        try {
            int count = processor.process();
            System.out.println("Processed " + count + " account records.");
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }
    }
}
