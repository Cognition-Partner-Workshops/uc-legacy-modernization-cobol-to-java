package com.carddemo.batch;

import com.carddemo.common.io.AccountRecordIO;
import com.carddemo.common.io.FixedWidthParser;
import com.carddemo.common.io.SequentialFileReader;
import com.carddemo.common.io.SequentialFileWriter;
import com.carddemo.common.model.AccountRecord;
import com.carddemo.common.util.AbendException;
import com.carddemo.common.util.DateConverter;

import java.math.BigDecimal;
import java.nio.file.Path;

/**
 * Migrated from COBOL program CBACT01C.cbl.
 * <p>
 * Reads the account master file (VSAM KSDS, sequential access) and writes
 * each account record into three different output files:
 * <ol>
 *   <li><b>OUTFILE</b> — Flat sequential file with selected account fields.
 *       The ACCT-CURR-CYC-DEBIT field defaults to 2525.00 if zero.</li>
 *   <li><b>ARRYFILE</b> — Array-structured record: account ID + 5 repeating
 *       groups (balance + cycle-debit in COMP-3), with only indices 1-3 populated.</li>
 *   <li><b>VBRCFILE</b> — Variable-length record file: two records per account
 *       (12-byte and 39-byte).</li>
 * </ol>
 */
public class Cbact01cJob {

    /** Display width of PIC S9(10)V99: 10 integer + 2 decimal = 12 bytes (sign via overpunch). */
    private static final int SIGNED_DEC_WIDTH = 12;

    private final Path acctFilePath;
    private final Path outFilePath;
    private final Path arryFilePath;
    private final Path vbrcFilePath;

    public Cbact01cJob(Path acctFilePath, Path outFilePath, Path arryFilePath, Path vbrcFilePath) {
        this.acctFilePath = acctFilePath;
        this.outFilePath = outFilePath;
        this.arryFilePath = arryFilePath;
        this.vbrcFilePath = vbrcFilePath;
    }

    public void execute() {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        try (var reader = new SequentialFileReader<>(acctFilePath, AccountRecordIO.PARSER);
             var outWriter = new SequentialFileWriter<String>(outFilePath, line -> line);
             var arryWriter = new SequentialFileWriter<String>(arryFilePath, line -> line);
             var vbrcWriter = new SequentialFileWriter<String>(vbrcFilePath, line -> line)) {

            for (AccountRecord account : reader) {
                displayAccountRecord(account);

                // 1. Populate and write OUT-ACCT-REC
                String outRecord = buildOutRecord(account);
                outWriter.write(outRecord);

                // 2. Populate and write ARR-ARRAY-REC
                String arryRecord = buildArrayRecord(account);
                arryWriter.write(arryRecord);

                // 3. Populate and write two variable-length VBRC records
                String vbr1 = buildVbrcRecord1(account);
                String vbr2 = buildVbrcRecord2(account);
                vbrcWriter.write(vbr1);
                vbrcWriter.write(vbr2);
            }

        } catch (AbendException e) {
            System.err.println("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
    }

    /**
     * Display account record fields (mirrors 1100-DISPLAY-ACCT-RECORD).
     */
    private void displayAccountRecord(AccountRecord acct) {
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
     * Build the OUTFILE record (mirrors 1300-POPUL-ACCT-RECORD).
     * Business rule: if ACCT-CURR-CYC-DEBIT == 0, default to 2525.00.
     * Date conversion: reissue date YYYY-MM-DD → YYYYMMDD via DateConverter.
     */
    String buildOutRecord(AccountRecord acct) {
        StringBuilder sb = new StringBuilder();
        sb.append(FixedWidthParser.formatNumeric(acct.acctId(), 11));
        sb.append(FixedWidthParser.formatAlpha(acct.activeStatus(), 1));
        sb.append(FixedWidthParser.formatSignedDecimalFixed(acct.currBal(), SIGNED_DEC_WIDTH, 2));
        sb.append(FixedWidthParser.formatSignedDecimalFixed(acct.creditLimit(), SIGNED_DEC_WIDTH, 2));
        sb.append(FixedWidthParser.formatSignedDecimalFixed(acct.cashCreditLimit(), SIGNED_DEC_WIDTH, 2));
        sb.append(FixedWidthParser.formatAlpha(acct.openDate(), 10));
        sb.append(FixedWidthParser.formatAlpha(acct.expirationDate(), 10));

        // Date conversion: YYYY-MM-DD to YYYYMMDD for reissue date
        String reissueDateCompact = DateConverter.toCompact(acct.reissueDate());
        sb.append(FixedWidthParser.formatAlpha(reissueDateCompact, 10));

        sb.append(FixedWidthParser.formatSignedDecimalFixed(acct.currCycCredit(), SIGNED_DEC_WIDTH, 2));

        // Business rule: default debit to 2525.00 if zero
        BigDecimal cycDebit = acct.currCycDebit();
        if (cycDebit.compareTo(BigDecimal.ZERO) == 0) {
            cycDebit = new BigDecimal("2525.00");
        }
        sb.append(FixedWidthParser.formatSignedDecimalFixed(cycDebit, SIGNED_DEC_WIDTH, 2));

        sb.append(FixedWidthParser.formatAlpha(acct.groupId(), 10));
        return sb.toString();
    }

    /**
     * Build the ARRYFILE record (mirrors 1400-POPUL-ARRAY-RECORD).
     * Array indices 1-3 are populated; 4-5 remain zeroed (INITIALIZE).
     * <pre>
     * Index 1: balance = ACCT-CURR-BAL, debit = 1005.00
     * Index 2: balance = ACCT-CURR-BAL, debit = 1525.00
     * Index 3: balance = -1025.00,      debit = -2500.00
     * Index 4-5: zeros
     * </pre>
     */
    String buildArrayRecord(AccountRecord acct) {
        StringBuilder sb = new StringBuilder();
        sb.append(FixedWidthParser.formatNumeric(acct.acctId(), 11));

        // Index 1
        sb.append(FixedWidthParser.formatSignedDecimalFixed(acct.currBal(), SIGNED_DEC_WIDTH, 2));
        sb.append(FixedWidthParser.formatSignedDecimalFixed(new BigDecimal("1005.00"), SIGNED_DEC_WIDTH, 2));
        // Index 2
        sb.append(FixedWidthParser.formatSignedDecimalFixed(acct.currBal(), SIGNED_DEC_WIDTH, 2));
        sb.append(FixedWidthParser.formatSignedDecimalFixed(new BigDecimal("1525.00"), SIGNED_DEC_WIDTH, 2));
        // Index 3
        sb.append(FixedWidthParser.formatSignedDecimalFixed(new BigDecimal("-1025.00"), SIGNED_DEC_WIDTH, 2));
        sb.append(FixedWidthParser.formatSignedDecimalFixed(new BigDecimal("-2500.00"), SIGNED_DEC_WIDTH, 2));
        // Index 4 (zeros)
        sb.append(FixedWidthParser.formatSignedDecimalFixed(BigDecimal.ZERO, SIGNED_DEC_WIDTH, 2));
        sb.append(FixedWidthParser.formatSignedDecimalFixed(BigDecimal.ZERO, SIGNED_DEC_WIDTH, 2));
        // Index 5 (zeros)
        sb.append(FixedWidthParser.formatSignedDecimalFixed(BigDecimal.ZERO, SIGNED_DEC_WIDTH, 2));
        sb.append(FixedWidthParser.formatSignedDecimalFixed(BigDecimal.ZERO, SIGNED_DEC_WIDTH, 2));

        // ARR-FILLER PIC X(04)
        sb.append(FixedWidthParser.filler(4));
        return sb.toString();
    }

    /**
     * Build VBRC record 1: 12 bytes (ACCT-ID + ACTIVE-STATUS).
     * Mirrors 1500-POPUL-VBRC-RECORD + 1550-WRITE-VB1-RECORD.
     */
    String buildVbrcRecord1(AccountRecord acct) {
        StringBuilder sb = new StringBuilder(12);
        sb.append(FixedWidthParser.formatNumeric(acct.acctId(), 11));
        sb.append(FixedWidthParser.formatAlpha(acct.activeStatus(), 1));
        return sb.toString();
    }

    /**
     * Build VBRC record 2: 39 bytes (ACCT-ID + CURR-BAL + CREDIT-LIMIT + REISSUE-YYYY).
     * Mirrors 1500-POPUL-VBRC-RECORD + 1575-WRITE-VB2-RECORD.
     */
    String buildVbrcRecord2(AccountRecord acct) {
        StringBuilder sb = new StringBuilder(39);
        sb.append(FixedWidthParser.formatNumeric(acct.acctId(), 11));
        sb.append(FixedWidthParser.formatSignedDecimalFixed(acct.currBal(), SIGNED_DEC_WIDTH, 2));
        sb.append(FixedWidthParser.formatSignedDecimalFixed(acct.creditLimit(), SIGNED_DEC_WIDTH, 2));

        // Extract the year from reissue date (YYYY-MM-DD → YYYY)
        String reissueDate = acct.reissueDate();
        String reissueYear = (reissueDate != null && reissueDate.length() >= 4)
                ? reissueDate.substring(0, 4) : "    ";
        sb.append(FixedWidthParser.formatAlpha(reissueYear, 4));
        return sb.toString();
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: Cbact01cJob <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }
        new Cbact01cJob(
                Path.of(args[0]), Path.of(args[1]),
                Path.of(args[2]), Path.of(args[3])
        ).execute();
    }
}
