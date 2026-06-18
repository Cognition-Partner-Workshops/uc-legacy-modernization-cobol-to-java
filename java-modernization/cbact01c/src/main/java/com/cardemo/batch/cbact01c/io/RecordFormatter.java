package com.cardemo.batch.cbact01c.io;

import com.cardemo.batch.cbact01c.model.ArrayRecord;
import com.cardemo.batch.cbact01c.model.OutputAccountRecord;
import com.cardemo.batch.cbact01c.model.VbRecord1;
import com.cardemo.batch.cbact01c.model.VbRecord2;
import com.cardemo.batch.cbact01c.util.ZonedDecimalParser;

import java.math.BigDecimal;

/**
 * Serialises the Java record types into fixed-width text lines that mirror
 * the COBOL FD record layouts.
 *
 * <p>COMP-3 (packed decimal) fields are represented as zoned-decimal text in
 * the modern output so they remain human-readable. The values are identical;
 * only the on-disk encoding differs from the mainframe binary format.
 */
public final class RecordFormatter {

    private RecordFormatter() {}

    public static String formatOutput(OutputAccountRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", r.acctId()));
        sb.append(r.activeStatus());
        sb.append(zonedS10V2(r.currBal()));
        sb.append(zonedS10V2(r.creditLimit()));
        sb.append(zonedS10V2(r.cashCreditLimit()));
        sb.append(padRight(r.openDate(), 10));
        sb.append(padRight(r.expirationDate(), 10));
        sb.append(padRight(r.reissueDate(), 10));
        sb.append(zonedS10V2(r.currCycCredit()));
        sb.append(zonedS10V2(r.currCycDebit()));
        sb.append(padRight(r.groupId(), 10));
        return sb.toString();
    }

    public static String formatArray(ArrayRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", r.acctId()));
        for (ArrayRecord.BalanceEntry entry : r.balanceEntries()) {
            sb.append(zonedS10V2(entry.currBal()));
            sb.append(zonedS10V2(entry.currCycDebit()));
        }
        sb.append(padRight(r.filler(), 4));
        return sb.toString();
    }

    public static String formatVb1(VbRecord1 r) {
        return String.format("%011d", r.acctId()) + r.activeStatus();
    }

    public static String formatVb2(VbRecord2 r) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", r.acctId()));
        sb.append(zonedS10V2(r.currBal()));
        sb.append(zonedS10V2(r.creditLimit()));
        sb.append(padRight(r.reissueYear(), 4));
        return sb.toString();
    }

    private static String zonedS10V2(BigDecimal value) {
        return ZonedDecimalParser.format(value, 12, 2);
    }

    private static String padRight(String s, int width) {
        if (s == null) return " ".repeat(width);
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }
}
