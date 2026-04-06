package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.io.ArrayFileWriter;
import com.carddemo.batch.io.OutputFileWriter;
import com.carddemo.batch.io.VbrFileWriter;
import com.carddemo.batch.model.*;
import com.carddemo.batch.model.ArrayAccountRecord.BalanceSlot;
import com.carddemo.batch.util.DateConverter;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ modernisation of COBOL batch program CBACT01C.
 * <p>
 * Reads an indexed account file (ACCTFILE) sequentially and writes
 * transformed records to three output files:
 * <ol>
 *   <li><b>OUTFILE</b>  – reformatted account record with date conversion
 *       and default-debit logic</li>
 *   <li><b>ARRYFILE</b> – array-structured record with balance slots</li>
 *   <li><b>VBRCFILE</b> – two variable-length records per account
 *       (short status record + longer balance record)</li>
 * </ol>
 *
 * <h3>Key business rules (from the COBOL source):</h3>
 * <ul>
 *   <li>Reissue date is converted from YYYY-MM-DD to YYYYMMDD
 *       (padded to 10 chars).</li>
 *   <li>If the current-cycle debit is zero, it defaults to 2525.00.</li>
 *   <li>Array slots 1-2 carry the account balance with hardcoded debits
 *       (1005.00 and 1525.00); slot 3 uses hardcoded negative values
 *       (-1025.00 bal, -2500.00 debit); slots 4-5 are zero-filled.</li>
 *   <li>VBR record 2 carries only the 4-digit year extracted from the
 *       reissue date.</li>
 * </ul>
 */
public final class Cbact01cProcessor {

    // Hardcoded constants from the COBOL source
    private static final BigDecimal DEFAULT_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARR_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARR_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARR_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARR_DEBIT_3 = new BigDecimal("-2500.00");

    private final Path inputPath;
    private final Path outFilePath;
    private final Path arrayFilePath;
    private final Path vbrFilePath;
    private final PrintStream console;

    /**
     * @param inputPath     path to the fixed-width account input file
     * @param outFilePath   path for the output account file
     * @param arrayFilePath path for the array output file
     * @param vbrFilePath   path for the variable-length record file
     * @param console       stream for DISPLAY statements (typically System.out)
     */
    public Cbact01cProcessor(Path inputPath,
                             Path outFilePath,
                             Path arrayFilePath,
                             Path vbrFilePath,
                             PrintStream console) {
        this.inputPath = inputPath;
        this.outFilePath = outFilePath;
        this.arrayFilePath = arrayFilePath;
        this.vbrFilePath = vbrFilePath;
        this.console = console;
    }

    /**
     * Executes the batch process – the equivalent of the COBOL PROCEDURE DIVISION.
     *
     * @return the number of records processed
     * @throws IOException on any file I/O error (equivalent to COBOL ABEND)
     */
    public int execute() throws IOException {
        console.println("START OF EXECUTION OF PROGRAM CBACT01C");

        int count = 0;

        try (var reader = new AccountFileReader(inputPath);
             var outWriter = new OutputFileWriter(outFilePath);
             var arrWriter = new ArrayFileWriter(arrayFilePath);
             var vbrWriter = new VbrFileWriter(vbrFilePath)) {

            AccountRecord record;
            while ((record = reader.readNext()) != null) {
                // 1100-DISPLAY-ACCT-RECORD
                console.println(record.toDisplayString());

                // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
                OutputAccountRecord outRec = populateOutputRecord(record);
                outWriter.write(outRec);

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                ArrayAccountRecord arrRec = populateArrayRecord(record);
                arrWriter.write(arrRec);

                // 1500-POPUL-VBRC-RECORD + 1550/1575-WRITE
                VbrRecord1 vb1 = populateVbrRecord1(record);
                VbrRecord2 vb2 = populateVbrRecord2(record);
                console.println("VBRC-REC1:" + vb1.acctId() + vb1.acctActiveStatus());
                console.println("VBRC-REC2:" + vb2.acctId() + vb2.acctCurrBal()
                        + vb2.acctCreditLimit() + vb2.acctReissueYyyy());
                vbrWriter.writeVb1(vb1);
                vbrWriter.writeVb2(vb2);

                count++;
            }
        }

        console.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return count;
    }

    // ---------------------------------------------------------------
    // 1300-POPUL-ACCT-RECORD
    // ---------------------------------------------------------------

    /**
     * Populates the output account record, applying the business rules:
     * <ul>
     *   <li>Convert reissue date from YYYY-MM-DD to YYYYMMDD (padded to 10 chars)</li>
     *   <li>Default cycle-debit to 2525.00 when input is zero</li>
     * </ul>
     */
    OutputAccountRecord populateOutputRecord(AccountRecord src) {
        // Date conversion: YYYY-MM-DD (type 2 input) → YYYYMMDD (type 2 output)
        String convertedReissueDate = DateConverter.convert(
                src.acctReissueDate(),
                DateConverter.TYPE_YYYY_MM_DD,
                DateConverter.TYPE_YYYYMMDD
        );

        // Default debit when zero
        BigDecimal cycDebit = src.acctCurrCycDebit();
        if (cycDebit.compareTo(BigDecimal.ZERO) == 0) {
            cycDebit = DEFAULT_DEBIT;
        }

        return new OutputAccountRecord(
                src.acctId(),
                src.acctActiveStatus(),
                src.acctCurrBal(),
                src.acctCreditLimit(),
                src.acctCashCreditLimit(),
                src.acctOpenDate(),
                src.acctExpirationDate(),
                convertedReissueDate,
                src.acctCurrCycCredit(),
                cycDebit,
                src.acctGroupId()
        );
    }

    // ---------------------------------------------------------------
    // 1400-POPUL-ARRAY-RECORD
    // ---------------------------------------------------------------

    /**
     * Populates the array record with balance slots per the COBOL logic:
     * <ul>
     *   <li>Slot 1: account balance, debit = 1005.00</li>
     *   <li>Slot 2: account balance, debit = 1525.00</li>
     *   <li>Slot 3: balance = -1025.00, debit = -2500.00</li>
     *   <li>Slots 4-5: zeroed (from INITIALIZE)</li>
     * </ul>
     */
    ArrayAccountRecord populateArrayRecord(AccountRecord src) {
        var slots = new ArrayList<BalanceSlot>(ArrayAccountRecord.SLOT_COUNT);
        slots.add(new BalanceSlot(src.acctCurrBal(), ARR_DEBIT_1));
        slots.add(new BalanceSlot(src.acctCurrBal(), ARR_DEBIT_2));
        slots.add(new BalanceSlot(ARR_BAL_3, ARR_DEBIT_3));
        slots.add(BalanceSlot.ZERO);
        slots.add(BalanceSlot.ZERO);
        return new ArrayAccountRecord(src.acctId(), List.copyOf(slots));
    }

    // ---------------------------------------------------------------
    // 1500-POPUL-VBRC-RECORD
    // ---------------------------------------------------------------

    VbrRecord1 populateVbrRecord1(AccountRecord src) {
        return new VbrRecord1(src.acctId(), src.acctActiveStatus());
    }

    VbrRecord2 populateVbrRecord2(AccountRecord src) {
        // Extract 4-digit year from YYYY-MM-DD reissue date
        String reissueYyyy = src.acctReissueDate().substring(0, 4);
        return new VbrRecord2(
                src.acctId(),
                src.acctCurrBal(),
                src.acctCreditLimit(),
                reissueYyyy
        );
    }
}
