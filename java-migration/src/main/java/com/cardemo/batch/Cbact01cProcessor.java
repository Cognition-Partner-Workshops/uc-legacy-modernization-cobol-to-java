package com.cardemo.batch;

import com.cardemo.batch.model.AccountRecord;
import com.cardemo.batch.model.ArrayRecord;
import com.cardemo.batch.model.ArrayRecord.BalanceSlot;
import com.cardemo.batch.model.OutputAccountRecord;
import com.cardemo.batch.model.VbRecord1;
import com.cardemo.batch.model.VbRecord2;
import com.cardemo.batch.util.DateConverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ migration of COBOL batch program CBACT01C.
 *
 * <p>Reads an account file (one fixed-length record per line),
 * then for each account writes:
 * <ol>
 *   <li>A flattened output account record to the output file</li>
 *   <li>An array-based record (5 balance/debit slots) to the array file</li>
 *   <li>Two variable-length records (short VB1, longer VB2) to the VB file</li>
 * </ol>
 */
public class Cbact01cProcessor {

    private static final BigDecimal DEFAULT_CYCLE_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARR_DEBIT_SLOT1 = new BigDecimal("1005.00");
    private static final BigDecimal ARR_DEBIT_SLOT2 = new BigDecimal("1525.00");
    private static final BigDecimal ARR_BAL_SLOT3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARR_DEBIT_SLOT3 = new BigDecimal("-2500.00");

    private final Path accountFilePath;
    private final Path outputFilePath;
    private final Path arrayFilePath;
    private final Path vbFilePath;
    private final PrintStream console;

    public Cbact01cProcessor(Path accountFilePath,
                             Path outputFilePath,
                             Path arrayFilePath,
                             Path vbFilePath,
                             PrintStream console) {
        this.accountFilePath = accountFilePath;
        this.outputFilePath = outputFilePath;
        this.arrayFilePath = arrayFilePath;
        this.vbFilePath = vbFilePath;
        this.console = console;
    }

    public Cbact01cProcessor(Path accountFilePath,
                             Path outputFilePath,
                             Path arrayFilePath,
                             Path vbFilePath) {
        this(accountFilePath, outputFilePath, arrayFilePath, vbFilePath, System.out);
    }

    public ProcessingResult execute() throws IOException {
        console.println("START OF EXECUTION OF PROGRAM CBACT01C");

        List<AccountRecord> processedAccounts = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(accountFilePath);
             BufferedWriter outWriter = Files.newBufferedWriter(outputFilePath);
             BufferedWriter arrWriter = Files.newBufferedWriter(arrayFilePath);
             BufferedWriter vbWriter = Files.newBufferedWriter(vbFilePath)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                AccountRecord account = AccountRecord.parse(line);
                processedAccounts.add(account);

                console.println(account.formatDisplay());

                OutputAccountRecord outRec = buildOutputRecord(account);
                outWriter.write(outRec.toDelimitedLine());
                outWriter.newLine();

                ArrayRecord arrRec = buildArrayRecord(account);
                arrWriter.write(arrRec.toDelimitedLine());
                arrWriter.newLine();

                VbRecord1 vb1 = buildVbRecord1(account);
                VbRecord2 vb2 = buildVbRecord2(account);
                console.println("VBRC-REC1:" + vb1.toDelimitedLine());
                console.println("VBRC-REC2:" + vb2.toDelimitedLine());

                vbWriter.write(vb1.toDelimitedLine());
                vbWriter.newLine();
                vbWriter.write(vb2.toDelimitedLine());
                vbWriter.newLine();
            }
        }

        console.println("END OF EXECUTION OF PROGRAM CBACT01C");

        return new ProcessingResult(processedAccounts.size());
    }

    OutputAccountRecord buildOutputRecord(AccountRecord account) {
        String convertedReissueDate = DateConverter.convert(
                account.reissueDate(), "2", "2"
        );

        BigDecimal cycleDebit = account.currentCycleDebit().compareTo(BigDecimal.ZERO) == 0
                ? DEFAULT_CYCLE_DEBIT
                : account.currentCycleDebit();

        return new OutputAccountRecord(
                account.acctId(),
                account.activeStatus(),
                account.currentBalance(),
                account.creditLimit(),
                account.cashCreditLimit(),
                account.openDate(),
                account.expirationDate(),
                convertedReissueDate,
                account.currentCycleCredit(),
                cycleDebit,
                account.groupId()
        );
    }

    ArrayRecord buildArrayRecord(AccountRecord account) {
        BalanceSlot[] slots = new BalanceSlot[ArrayRecord.SLOT_COUNT];
        slots[0] = new BalanceSlot(account.currentBalance(), ARR_DEBIT_SLOT1);
        slots[1] = new BalanceSlot(account.currentBalance(), ARR_DEBIT_SLOT2);
        slots[2] = new BalanceSlot(ARR_BAL_SLOT3, ARR_DEBIT_SLOT3);
        slots[3] = new BalanceSlot(BigDecimal.ZERO, BigDecimal.ZERO);
        slots[4] = new BalanceSlot(BigDecimal.ZERO, BigDecimal.ZERO);
        return new ArrayRecord(account.acctId(), slots);
    }

    VbRecord1 buildVbRecord1(AccountRecord account) {
        return new VbRecord1(account.acctId(), account.activeStatus());
    }

    VbRecord2 buildVbRecord2(AccountRecord account) {
        String reissueYear = "";
        String reissueDate = account.reissueDate();
        if (reissueDate != null && reissueDate.length() >= 4) {
            reissueYear = reissueDate.substring(0, 4);
        }
        return new VbRecord2(
                account.acctId(),
                account.currentBalance(),
                account.creditLimit(),
                reissueYear
        );
    }

    public record ProcessingResult(int recordsProcessed) {}

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: Cbact01cProcessor <acctFile> <outFile> <arrFile> <vbFile>");
            System.exit(1);
        }

        Cbact01cProcessor processor = new Cbact01cProcessor(
                Path.of(args[0]),
                Path.of(args[1]),
                Path.of(args[2]),
                Path.of(args[3])
        );

        try {
            ProcessingResult result = processor.execute();
            System.out.printf("Processed %d account records.%n", result.recordsProcessed());
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM");
            System.err.println("FILE STATUS IS: " + e.getMessage());
            System.exit(999);
        }
    }
}
