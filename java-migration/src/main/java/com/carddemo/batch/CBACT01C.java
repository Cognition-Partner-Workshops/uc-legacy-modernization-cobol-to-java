package com.carddemo.batch;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

/**
 * Java 17+ migration of COBOL batch program CBACT01C.
 *
 * <b>Original purpose:</b> Read the Account VSAM KSDS file sequentially and
 * produce three output files (fixed-length account extract, array record file,
 * and variable-length record file).
 *
 * <h3>Business Logic Summary</h3>
 * <ol>
 *   <li>Open the indexed account input file for sequential read.</li>
 *   <li>For each account record:
 *     <ul>
 *       <li>Display all fields to the console (audit trail).</li>
 *       <li>Build and write a fixed-length output record (OUTFILE):
 *         <ul>
 *           <li>Reformat the reissue date from YYYY-MM-DD to YYYYMMDD
 *               via the COBDATFT assembler routine (Java: {@link DateFormatter}).</li>
 *           <li>If cycle debit is zero, substitute 2525.00.</li>
 *           <li>Cycle debit is stored as COMP-3 (packed decimal).</li>
 *         </ul>
 *       </li>
 *       <li>Build and write an array record (ARRYFILE) with 5 groups of
 *           balance/debit pairs using hardcoded test values.</li>
 *       <li>Build and write two variable-length records (VBRCFILE):
 *           VB1 (ID + status, 12 bytes) and VB2 (ID + bal + limit + year, 39 bytes).</li>
 *     </ul>
 *   </li>
 *   <li>Close all files and display completion message.</li>
 * </ol>
 */
public class CBACT01C {

    private static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");

    private final Path inputFile;
    private final Path outFile;
    private final Path arryFile;
    private final Path vbrcFile;
    private final PrintStream console;

    /**
     * Construct the batch processor with explicit file paths.
     */
    public CBACT01C(Path inputFile, Path outFile, Path arryFile, Path vbrcFile,
                    PrintStream console) {
        this.inputFile = inputFile;
        this.outFile = outFile;
        this.arryFile = arryFile;
        this.vbrcFile = vbrcFile;
        this.console = console;
    }

    /**
     * Execute the batch job, mirroring the PROCEDURE DIVISION of the COBOL program.
     */
    public void execute() throws IOException {
        console.println("START OF EXECUTION OF PROGRAM CBACT01C");

        List<AccountRecord> accounts = AccountFileParser.parse(inputFile);

        try (OutputRecordWriter writer = new OutputRecordWriter(outFile, arryFile, vbrcFile)) {
            for (AccountRecord acct : accounts) {
                // 1100-DISPLAY-ACCT-RECORD
                displayAccountRecord(acct);

                // 1300-POPUL-ACCT-RECORD: date formatting + debit substitution
                String formattedReissueDate = DateFormatter.formatDate(
                        acct.acctReissueDate(), '2', '2');

                BigDecimal cycDebitValue = acct.acctCurrCycDebit().signum() == 0
                        ? DEFAULT_CYC_DEBIT
                        : acct.acctCurrCycDebit();

                // 1350-WRITE-ACCT-RECORD
                writer.writeOutRecord(acct, formattedReissueDate, cycDebitValue);

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                writer.writeArryRecord(acct);

                // 1500-POPUL-VBRC-RECORD + 1550/1575-WRITE-VB1/VB2-RECORD
                writer.writeVbrcRecords(acct);

                // Display the full account record (matches COBOL: DISPLAY ACCOUNT-RECORD)
                console.println(formatAccountRecordDisplay(acct));
            }
        }

        console.println("END OF EXECUTION OF PROGRAM CBACT01C");
    }

    /**
     * Display individual account fields, matching 1100-DISPLAY-ACCT-RECORD.
     */
    private void displayAccountRecord(AccountRecord acct) {
        console.println("ACCT-ID                 :" + CobolDecimalUtils.formatUnsignedNumeric(acct.acctId(), 11));
        console.println("ACCT-ACTIVE-STATUS      :" + acct.acctActiveStatus());
        console.println("ACCT-CURR-BAL           :" + CobolDecimalUtils.formatZonedDecimal(acct.acctCurrBal(), 12, 2));
        console.println("ACCT-CREDIT-LIMIT       :" + CobolDecimalUtils.formatZonedDecimal(acct.acctCreditLimit(), 12, 2));
        console.println("ACCT-CASH-CREDIT-LIMIT  :" + CobolDecimalUtils.formatZonedDecimal(acct.acctCashCreditLimit(), 12, 2));
        console.println("ACCT-OPEN-DATE          :" + acct.acctOpenDate());
        console.println("ACCT-EXPIRAION-DATE     :" + acct.acctExpirationDate());
        console.println("ACCT-REISSUE-DATE       :" + acct.acctReissueDate());
        console.println("ACCT-CURR-CYC-CREDIT    :" + CobolDecimalUtils.formatZonedDecimal(acct.acctCurrCycCredit(), 12, 2));
        console.println("ACCT-CURR-CYC-DEBIT     :" + CobolDecimalUtils.formatZonedDecimal(acct.acctCurrCycDebit(), 12, 2));
        console.println("ACCT-GROUP-ID           :" + acct.acctGroupId());
        console.println("-------------------------------------------------");
    }

    /**
     * Format the raw 300-byte ACCOUNT-RECORD display line.
     * This matches the COBOL: DISPLAY ACCOUNT-RECORD at line 151.
     */
    private String formatAccountRecordDisplay(AccountRecord acct) {
        StringBuilder sb = new StringBuilder(300);
        sb.append(CobolDecimalUtils.formatUnsignedNumeric(acct.acctId(), 11));
        sb.append(acct.acctActiveStatus());
        sb.append(CobolDecimalUtils.formatZonedDecimal(acct.acctCurrBal(), 12, 2));
        sb.append(CobolDecimalUtils.formatZonedDecimal(acct.acctCreditLimit(), 12, 2));
        sb.append(CobolDecimalUtils.formatZonedDecimal(acct.acctCashCreditLimit(), 12, 2));
        sb.append(acct.acctOpenDate());
        sb.append(acct.acctExpirationDate());
        sb.append(acct.acctReissueDate());
        sb.append(CobolDecimalUtils.formatZonedDecimal(acct.acctCurrCycCredit(), 12, 2));
        sb.append(CobolDecimalUtils.formatZonedDecimal(acct.acctCurrCycDebit(), 12, 2));
        sb.append(CobolDecimalUtils.fixedWidth(acct.acctAddrZip(), 10));
        sb.append(CobolDecimalUtils.fixedWidth(acct.acctGroupId(), 10));
        // Pad to 300 bytes
        while (sb.length() < 300) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * CLI entry point.  Usage:
     *   java com.carddemo.batch.CBACT01C &lt;acctfile&gt; &lt;outfile&gt; &lt;arryfile&gt; &lt;vbrcfile&gt;
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: CBACT01C <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }
        CBACT01C job = new CBACT01C(
                Path.of(args[0]),
                Path.of(args[1]),
                Path.of(args[2]),
                Path.of(args[3]),
                System.out
        );
        job.execute();
    }
}
