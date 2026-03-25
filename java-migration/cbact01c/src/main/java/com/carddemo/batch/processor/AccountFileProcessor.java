package com.carddemo.batch.processor;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.io.AccountFileWriter;
import com.carddemo.batch.model.*;
import com.carddemo.batch.util.DateFormatter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ rewrite of COBOL batch program CBACT01C.
 *
 * <h2>Original COBOL Purpose</h2>
 * Reads an account VSAM KSDS file sequentially and writes each record
 * to three output files:
 * <ol>
 *   <li><b>OUT-FILE</b> — account summary with date-reformatted reissue date
 *       and conditional debit override</li>
 *   <li><b>ARRY-FILE</b> — five-occurrence array record with hardcoded test
 *       values in positions 1-3</li>
 *   <li><b>VBRC-FILE</b> — two variable-length records per account: a short
 *       (12-byte) ID + status, and a longer (39-byte) ID + balance + credit
 *       limit + reissue year</li>
 * </ol>
 *
 * <h2>Key Business Rules Preserved</h2>
 * <ul>
 *   <li>If {@code ACCT-CURR-CYC-DEBIT == 0}, override to {@code 2525.00}</li>
 *   <li>Array record entries 4 and 5 are always zero-initialized</li>
 *   <li>Hardcoded array values: 1005.00, 1525.00, -1025.00, -2500.00</li>
 *   <li>Reissue date is converted from YYYY-MM-DD to YYYYMMDD via COBDATFT
 *       (now {@link DateFormatter})</li>
 * </ul>
 */
public final class AccountFileProcessor {

    /** Hardcoded debit value when original debit is zero (COBOL: MOVE 2525.00) */
    private static final BigDecimal DEBIT_OVERRIDE = new BigDecimal("2525.00");

    /** Hardcoded array debit values per COBOL source lines 256-260 */
    private static final BigDecimal ARR_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARR_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARR_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARR_DEBIT_3 = new BigDecimal("-2500.00");

    private final Path inputFile;
    private final Path outFile;
    private final Path arryFile;
    private final Path vbrcFile;

    public AccountFileProcessor(Path inputFile, Path outFile, Path arryFile, Path vbrcFile) {
        this.inputFile = inputFile;
        this.outFile = outFile;
        this.arryFile = arryFile;
        this.vbrcFile = vbrcFile;
    }

    /**
     * Execute the batch process — mirrors the COBOL PROCEDURE DIVISION main loop.
     *
     * @return number of records processed
     */
    public int process() throws IOException {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        int count = 0;
        try (var reader = new AccountFileReader(inputFile);
             var writer = new AccountFileWriter(outFile, arryFile, vbrcFile)) {

            List<AccountRecord> records = reader.readAll();

            for (var acctRecord : records) {
                System.out.println(acctRecord);

                // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
                var outRecord = populateOutRecord(acctRecord);
                writer.writeOutRecord(outRecord);

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                var arrayRecord = populateArrayRecord(acctRecord);
                writer.writeArrayRecord(arrayRecord);

                // 1500-POPUL-VBRC-RECORD
                var vbr1 = populateVbrRecord1(acctRecord);
                var vbr2 = populateVbrRecord2(acctRecord);

                // 1550-WRITE-VB1-RECORD + 1575-WRITE-VB2-RECORD
                writer.writeVbrRecord1(vbr1);
                writer.writeVbrRecord2(vbr2);

                count++;
            }
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return count;
    }

    /**
     * Mirrors COBOL paragraph 1300-POPUL-ACCT-RECORD.
     *
     * Business rules:
     * <ul>
     *   <li>Reissue date is converted from YYYY-MM-DD → YYYYMMDD via date formatter</li>
     *   <li>If current-cycle-debit is zero, override to 2525.00</li>
     * </ul>
     */
    OutAccountRecord populateOutRecord(AccountRecord acct) {
        // Date conversion: YYYY-MM-DD → YYYYMMDD (COBDATFT replacement)
        String reissueDate = acct.reissueDate();
        var dateResult = DateFormatter.convert("2", reissueDate, "2");
        if (dateResult.isSuccess()) {
            reissueDate = dateResult.outputDate();
        }
        // else: keep original date (COBOL would set error message but continue)

        // Business rule: override zero debit
        BigDecimal cycDebit = acct.currentCycleDebit();
        if (cycDebit.compareTo(BigDecimal.ZERO) == 0) {
            cycDebit = DEBIT_OVERRIDE;
        }

        return new OutAccountRecord(
                acct.acctId(),
                acct.activeStatus(),
                acct.currentBalance(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                reissueDate,
                acct.currentCycleCredit(),
                cycDebit,
                acct.groupId()
        );
    }

    /**
     * Mirrors COBOL paragraph 1400-POPUL-ARRAY-RECORD.
     *
     * Populates a 5-element array:
     * <ul>
     *   <li>Index 0: balance = acct balance, debit = 1005.00</li>
     *   <li>Index 1: balance = acct balance, debit = 1525.00</li>
     *   <li>Index 2: balance = -1025.00, debit = -2500.00</li>
     *   <li>Index 3-4: zero-initialized (COBOL: INITIALIZE ARR-ARRAY-REC)</li>
     * </ul>
     */
    ArrayRecord populateArrayRecord(AccountRecord acct) {
        var entries = new ArrayList<ArrayRecord.BalanceEntry>(5);
        entries.add(new ArrayRecord.BalanceEntry(acct.currentBalance(), ARR_DEBIT_1));
        entries.add(new ArrayRecord.BalanceEntry(acct.currentBalance(), ARR_DEBIT_2));
        entries.add(new ArrayRecord.BalanceEntry(ARR_BAL_3, ARR_DEBIT_3));
        entries.add(ArrayRecord.BalanceEntry.ZERO);
        entries.add(ArrayRecord.BalanceEntry.ZERO);
        return new ArrayRecord(acct.acctId(), List.copyOf(entries));
    }

    /**
     * Mirrors COBOL paragraph 1500-POPUL-VBRC-RECORD (VBR record 1).
     */
    VbrRecord1 populateVbrRecord1(AccountRecord acct) {
        return new VbrRecord1(acct.acctId(), acct.activeStatus());
    }

    /**
     * Mirrors COBOL paragraph 1500-POPUL-VBRC-RECORD (VBR record 2).
     *
     * The reissue year is extracted from the original YYYY-MM-DD date
     * (COBOL: WS-ACCT-REISSUE-YYYY, which is the first 4 chars of the
     * reissue date that was stored in WS-REISSUE-DATE before COBDATFT call).
     */
    VbrRecord2 populateVbrRecord2(AccountRecord acct) {
        // Extract year from YYYY-MM-DD format (original date, not converted)
        String reissueYear = "";
        if (acct.reissueDate() != null && acct.reissueDate().length() >= 4) {
            reissueYear = acct.reissueDate().substring(0, 4);
        }
        return new VbrRecord2(
                acct.acctId(),
                acct.currentBalance(),
                acct.creditLimit(),
                reissueYear
        );
    }

    /**
     * CLI entry point — mirrors COBOL JCL invocation.
     *
     * Usage: java AccountFileProcessor &lt;input&gt; &lt;out&gt; &lt;arry&gt; &lt;vbrc&gt;
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: AccountFileProcessor <input> <outFile> <arryFile> <vbrcFile>");
            System.exit(1);
        }

        var processor = new AccountFileProcessor(
                Path.of(args[0]),
                Path.of(args[1]),
                Path.of(args[2]),
                Path.of(args[3])
        );

        int count = processor.process();
        System.out.printf("Processed %d account records.%n", count);
    }
}
