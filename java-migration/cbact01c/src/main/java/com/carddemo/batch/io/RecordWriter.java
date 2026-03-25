package com.carddemo.batch.io;

import com.carddemo.batch.model.ArrayAccountRecord;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VbrcRecord1;
import com.carddemo.batch.model.VbrcRecord2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Writes migrated account records to output files in delimited text format.
 *
 * <p>The COBOL version writes fixed-width binary records (including COMP-3
 * packed decimal). The Java version writes pipe-delimited text for
 * interoperability with modern systems. Field order and semantics are
 * preserved exactly.
 */
public final class RecordWriter {

    /** Field separator for output files. */
    public static final String DELIMITER = "|";

    private RecordWriter() {}

    /**
     * Write an OutputAccountRecord to the output file.
     * Field order matches the COBOL OUT-ACCT-REC layout.
     */
    public static void writeOutputRecord(BufferedWriter writer,
                                         OutputAccountRecord rec) throws IOException {
        writer.write(String.join(DELIMITER,
                String.valueOf(rec.acctId()),
                rec.activeStatus(),
                rec.currBal().toPlainString(),
                rec.creditLimit().toPlainString(),
                rec.cashCreditLimit().toPlainString(),
                rec.openDate(),
                rec.expirationDate(),
                rec.reissueDate(),
                rec.currCycCredit().toPlainString(),
                rec.currCycDebit().toPlainString(),
                rec.groupId()
        ));
        writer.newLine();
    }

    /**
     * Write an ArrayAccountRecord to the array output file.
     * Each line: acctId | bal1 | debit1 | bal2 | debit2 | ... | bal5 | debit5
     */
    public static void writeArrayRecord(BufferedWriter writer,
                                        ArrayAccountRecord rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(rec.acctId());
        for (ArrayAccountRecord.BalanceEntry entry : rec.entries()) {
            sb.append(DELIMITER).append(entry.currBal().toPlainString());
            sb.append(DELIMITER).append(entry.currCycDebit().toPlainString());
        }
        writer.write(sb.toString());
        writer.newLine();
    }

    /**
     * Write a VbrcRecord1 (short variable-length record) to the VBRC output file.
     */
    public static void writeVbrc1(BufferedWriter writer,
                                  VbrcRecord1 rec) throws IOException {
        writer.write(String.join(DELIMITER,
                String.valueOf(rec.acctId()),
                rec.activeStatus()
        ));
        writer.newLine();
    }

    /**
     * Write a VbrcRecord2 (long variable-length record) to the VBRC output file.
     */
    public static void writeVbrc2(BufferedWriter writer,
                                  VbrcRecord2 rec) throws IOException {
        writer.write(String.join(DELIMITER,
                String.valueOf(rec.acctId()),
                rec.currBal().toPlainString(),
                rec.creditLimit().toPlainString(),
                rec.reissueYyyy()
        ));
        writer.newLine();
    }
}
