package com.carddemo.statement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Emulation of the {@code CREASTMT.JCL} STEP010 SORT that turns the 350-byte
 * TRANSACT records of {@code dailytran.txt} into the {@code COSTM01} reporting layout:
 *
 * <pre>
 *   SORT FIELDS=(263,16,CH,A,1,16,CH,A)
 *   OUTREC FIELDS=(1:263,16, 17:1,262, 279:279,50)
 * </pre>
 */
public final class JclTransactionSort {

    private static final int TRANSACT_LENGTH = 350;

    private JclTransactionSort() {
    }

    /** Reshapes then sorts by card number, then transaction id. */
    public static List<TransactionRecord> reshapeAndSort(List<String> transactRecords) {
        List<TransactionRecord> reshaped = new ArrayList<>(transactRecords.size());
        for (String raw : transactRecords) {
            reshaped.add(TransactionRecord.parse(reshape(raw)));
        }
        reshaped.sort(Comparator.comparing(TransactionRecord::cardNumber)
                .thenComparing(TransactionRecord::transactionId));
        return reshaped;
    }

    /** Applies the OUTREC field mapping to a single TRANSACT record. */
    public static String reshape(String transactRecord) {
        String rec = CobolText.alphanumeric(transactRecord, TRANSACT_LENGTH);
        String cardNumber = CobolText.field(rec, 262, 16);
        String head = CobolText.field(rec, 0, 262);
        String tail = CobolText.field(rec, 278, 50);
        return CobolText.alphanumeric(cardNumber + head + tail, TransactionRecord.RECORD_LENGTH);
    }
}
