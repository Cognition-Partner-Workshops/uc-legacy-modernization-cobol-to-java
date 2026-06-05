package com.carddemo.batch;

import com.carddemo.batch.model.*;
import com.carddemo.batch.model.ArrayAccountRecord.BalanceEntry;
import com.carddemo.batch.util.DateConverter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Java equivalent of the COBOL batch program {@code CBACT01C}.
 *
 * <h2>Business logic</h2>
 * <ol>
 *   <li>Read every record from an indexed VSAM account file (here: a
 *       fixed-width flat file).</li>
 *   <li>For each account, produce three output artefacts:
 *     <ul>
 *       <li><b>Out-file record</b> — selected fields with a reformatted
 *           reissue date (YYYY-MM-DD → YYYYMMDD) and a special rule:
 *           if {@code currCycDebit == 0} it is replaced with 2525.00.</li>
 *       <li><b>Array-file record</b> — account ID with a 5-element balance
 *           array; elements 1-2 use the account's current balance, element 3
 *           uses hardcoded negatives, and elements 4-5 are zeroes.</li>
 *       <li><b>Two variable-length records</b> — a short record (ID + status)
 *           and a longer record (ID + balance + credit limit + reissue
 *           year).</li>
 *     </ul>
 *   </li>
 * </ol>
 */
public final class CbAct01CProcessor {

    private static final BigDecimal DEBIT_SUBSTITUTION = new BigDecimal("2525.00");

    private CbAct01CProcessor() {}

    // ---- transformation methods (pure, testable) ----

    public static OutAccountRecord buildOutRecord(AccountRecord acct) {
        String reformattedDate = DateConverter.convert(
                acct.reissueDate(), "2", "2");

        BigDecimal debit = acct.currCycDebit().signum() == 0
                ? DEBIT_SUBSTITUTION
                : acct.currCycDebit();

        return new OutAccountRecord(
                acct.acctId(),
                acct.activeStatus(),
                acct.currBal(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                reformattedDate,
                acct.currCycCredit(),
                debit,
                acct.groupId());
    }

    public static ArrayAccountRecord buildArrayRecord(AccountRecord acct) {
        List<BalanceEntry> entries = new ArrayList<>(ArrayAccountRecord.OCCURS_COUNT);
        entries.add(new BalanceEntry(acct.currBal(), new BigDecimal("1005.00")));
        entries.add(new BalanceEntry(acct.currBal(), new BigDecimal("1525.00")));
        entries.add(new BalanceEntry(new BigDecimal("-1025.00"), new BigDecimal("-2500.00")));
        entries.add(new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO));
        entries.add(new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO));

        return new ArrayAccountRecord(
                acct.acctId(),
                Collections.unmodifiableList(entries),
                "    ");
    }

    public static VbRecord1 buildVbRecord1(AccountRecord acct) {
        return new VbRecord1(acct.acctId(), acct.activeStatus());
    }

    public static VbRecord2 buildVbRecord2(AccountRecord acct) {
        String reissueYear = acct.reissueDate().substring(0, 4);
        return new VbRecord2(
                acct.acctId(),
                acct.currBal(),
                acct.creditLimit(),
                reissueYear);
    }

    // ---- batch orchestration ----

    /**
     * Run the full batch: read the input file, transform each record, and
     * write the three output files.
     *
     * @return the number of records processed
     */
    public static int execute(Path inputFile, Path outFile,
                              Path arrayFile, Path vbFile) throws IOException {
        List<AccountRecord> accounts = AccountFileReader.readAll(inputFile);

        try (BufferedWriter outWriter   = Files.newBufferedWriter(outFile);
             BufferedWriter arrWriter   = Files.newBufferedWriter(arrayFile);
             BufferedWriter vbWriter    = Files.newBufferedWriter(vbFile)) {

            for (AccountRecord acct : accounts) {
                OutAccountRecord out = buildOutRecord(acct);
                writeOutRecord(outWriter, out);

                ArrayAccountRecord arr = buildArrayRecord(acct);
                writeArrayRecord(arrWriter, arr);

                VbRecord1 vb1 = buildVbRecord1(acct);
                VbRecord2 vb2 = buildVbRecord2(acct);
                writeVbRecord1(vbWriter, vb1);
                writeVbRecord2(vbWriter, vb2);
            }
        }
        return accounts.size();
    }

    // ---- CSV serialisation (modern replacement for fixed-width output) ----

    static void writeOutRecord(BufferedWriter w, OutAccountRecord r)
            throws IOException {
        w.write(String.join(",",
                String.valueOf(r.acctId()),
                r.activeStatus(),
                r.currBal().toPlainString(),
                r.creditLimit().toPlainString(),
                r.cashCreditLimit().toPlainString(),
                r.openDate(),
                r.expirationDate(),
                r.reissueDate(),
                r.currCycCredit().toPlainString(),
                r.currCycDebit().toPlainString(),
                r.groupId()));
        w.newLine();
    }

    static void writeArrayRecord(BufferedWriter w, ArrayAccountRecord r)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(r.acctId());
        for (BalanceEntry e : r.balanceEntries()) {
            sb.append(',').append(e.currBal().toPlainString());
            sb.append(',').append(e.currCycDebit().toPlainString());
        }
        w.write(sb.toString());
        w.newLine();
    }

    static void writeVbRecord1(BufferedWriter w, VbRecord1 r)
            throws IOException {
        w.write(String.join(",",
                "VB1",
                String.valueOf(r.acctId()),
                r.activeStatus()));
        w.newLine();
    }

    static void writeVbRecord2(BufferedWriter w, VbRecord2 r)
            throws IOException {
        w.write(String.join(",",
                "VB2",
                String.valueOf(r.acctId()),
                r.currBal().toPlainString(),
                r.creditLimit().toPlainString(),
                r.reissueYear()));
        w.newLine();
    }
}
