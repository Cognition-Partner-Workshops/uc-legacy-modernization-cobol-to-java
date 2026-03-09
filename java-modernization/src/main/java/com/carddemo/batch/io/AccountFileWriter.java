package com.carddemo.batch.io;

import com.carddemo.batch.model.ArrayAccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord.BalanceEntry;
import com.carddemo.batch.model.OutAccountRecord;
import com.carddemo.batch.model.VbRecord1;
import com.carddemo.batch.model.VbRecord2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the three output files produced by the CBACT01C batch program.
 * <p>
 * Each writer outputs CSV lines for easy verification, with field values
 * matching the COBOL output semantics exactly.
 * <p>
 * Corresponds to COBOL paragraphs:
 * <ul>
 *   <li>2000-OUTFILE-OPEN / 1350-WRITE-ACCT-RECORD</li>
 *   <li>3000-ARRFILE-OPEN / 1450-WRITE-ARRY-RECORD</li>
 *   <li>4000-VBRFILE-OPEN / 1550-WRITE-VB1-RECORD / 1575-WRITE-VB2-RECORD</li>
 * </ul>
 */
public final class AccountFileWriter implements AutoCloseable {

    private static final String SEPARATOR = ",";

    private final BufferedWriter outWriter;
    private final BufferedWriter arryWriter;
    private final BufferedWriter vbrcWriter;

    public AccountFileWriter(Path outFilePath, Path arryFilePath, Path vbrcFilePath)
            throws IOException {
        this.outWriter  = Files.newBufferedWriter(outFilePath);
        this.arryWriter = Files.newBufferedWriter(arryFilePath);
        this.vbrcWriter = Files.newBufferedWriter(vbrcFilePath);
    }

    /**
     * Writes a single output account record (OUTFILE).
     */
    public void writeOutRecord(OutAccountRecord rec) throws IOException {
        outWriter.write(String.join(SEPARATOR,
                String.valueOf(rec.acctId()),
                rec.acctActiveStatus(),
                rec.acctCurrBal().toPlainString(),
                rec.acctCreditLimit().toPlainString(),
                rec.acctCashCreditLimit().toPlainString(),
                rec.acctOpenDate(),
                rec.acctExpiraionDate(),
                rec.acctReissueDate(),
                rec.acctCurrCycCredit().toPlainString(),
                rec.acctCurrCycDebit().toPlainString(),
                rec.acctGroupId()
        ));
        outWriter.newLine();
    }

    /**
     * Writes a single array account record (ARRYFILE).
     */
    public void writeArrayRecord(ArrayAccountRecord rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(rec.acctId());
        for (BalanceEntry entry : rec.balanceEntries()) {
            sb.append(SEPARATOR).append(entry.currBal().toPlainString());
            sb.append(SEPARATOR).append(entry.currCycDebit().toPlainString());
        }
        arryWriter.write(sb.toString());
        arryWriter.newLine();
    }

    /**
     * Writes a VB record type 1 (short record) to the VBRC output file.
     */
    public void writeVbRecord1(VbRecord1 rec) throws IOException {
        vbrcWriter.write(String.join(SEPARATOR,
                "VB1",
                String.valueOf(rec.acctId()),
                rec.acctActiveStatus()
        ));
        vbrcWriter.newLine();
    }

    /**
     * Writes a VB record type 2 (long record) to the VBRC output file.
     */
    public void writeVbRecord2(VbRecord2 rec) throws IOException {
        vbrcWriter.write(String.join(SEPARATOR,
                "VB2",
                String.valueOf(rec.acctId()),
                rec.acctCurrBal().toPlainString(),
                rec.acctCreditLimit().toPlainString(),
                rec.acctReissueYear()
        ));
        vbrcWriter.newLine();
    }

    @Override
    public void close() throws IOException {
        IOException firstEx = null;
        for (BufferedWriter w : new BufferedWriter[]{outWriter, arryWriter, vbrcWriter}) {
            try {
                w.close();
            } catch (IOException e) {
                if (firstEx == null) firstEx = e;
                else firstEx.addSuppressed(e);
            }
        }
        if (firstEx != null) throw firstEx;
    }
}
