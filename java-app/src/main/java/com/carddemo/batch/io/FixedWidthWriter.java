package com.carddemo.batch.io;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * Writes fixed-width records mixing DISPLAY and COMP-3 fields,
 * mirroring COBOL sequential WRITE semantics.
 */
public final class FixedWidthWriter implements AutoCloseable {

    private final OutputStream out;

    public FixedWidthWriter(OutputStream out) {
        this.out = new BufferedOutputStream(out);
    }

    /**
     * Write a complete OUT-ACCT-REC record (107 bytes).
     *
     * Layout:
     *   OUT-ACCT-ID                  PIC 9(11)          11
     *   OUT-ACCT-ACTIVE-STATUS       PIC X(01)           1
     *   OUT-ACCT-CURR-BAL            PIC S9(10)V99      12
     *   OUT-ACCT-CREDIT-LIMIT        PIC S9(10)V99      12
     *   OUT-ACCT-CASH-CREDIT-LIMIT   PIC S9(10)V99      12
     *   OUT-ACCT-OPEN-DATE           PIC X(10)          10
     *   OUT-ACCT-EXPIRAION-DATE      PIC X(10)          10
     *   OUT-ACCT-REISSUE-DATE        PIC X(10)          10
     *   OUT-ACCT-CURR-CYC-CREDIT     PIC S9(10)V99      12
     *   OUT-ACCT-CURR-CYC-DEBIT      PIC S9(10)V99 COMP-3  7
     *   OUT-ACCT-GROUP-ID            PIC X(10)          10
     *                                                  107
     */
    public void writeOutRecord(String acctId, String activeStatus,
                               BigDecimal currBal, BigDecimal creditLimit,
                               BigDecimal cashCreditLimit,
                               String openDate, String expiraionDate,
                               String reissueDate,
                               BigDecimal currCycCredit, BigDecimal currCycDebit,
                               String groupId) throws IOException {
        writeDisplay9(acctId, 11);
        writeAlpha(activeStatus, 1);
        writeZoned(currBal, 12, 2);
        writeZoned(creditLimit, 12, 2);
        writeZoned(cashCreditLimit, 12, 2);
        writeAlpha(openDate, 10);
        writeAlpha(expiraionDate, 10);
        writeAlpha(reissueDate, 10);
        writeZoned(currCycCredit, 12, 2);
        writePacked(currCycDebit, 12, 2);
        writeAlpha(groupId, 10);
    }

    /**
     * Write a complete ARR-ARRAY-REC record (110 bytes).
     *
     * Layout:
     *   ARR-ACCT-ID              PIC 9(11)           11
     *   ARR-ACCT-BAL OCCURS 5:
     *     ARR-ACCT-CURR-BAL      PIC S9(10)V99       12  x5
     *     ARR-ACCT-CURR-CYC-DEBIT PIC S9(10)V99 COMP-3 7  x5
     *   ARR-FILLER               PIC X(04)            4
     *                                                110
     */
    public void writeArrayRecord(String acctId,
                                 BigDecimal[] balances,
                                 BigDecimal[] debits) throws IOException {
        writeDisplay9(acctId, 11);
        for (int i = 0; i < 5; i++) {
            writeZoned(balances[i], 12, 2);
            writePacked(debits[i], 12, 2);
        }
        writeAlpha("", 4);
    }

    /**
     * Write a variable-length record: the content bytes followed by a newline.
     */
    public void writeVariableRecord(byte[] content) throws IOException {
        out.write(content);
        out.write('\n');
    }

    private void writeDisplay9(String value, int len) throws IOException {
        String padded = padRight(value, len);
        out.write(padded.getBytes(StandardCharsets.ISO_8859_1), 0, len);
    }

    private void writeAlpha(String value, int len) throws IOException {
        String padded = padRight(value, len);
        out.write(padded.getBytes(StandardCharsets.ISO_8859_1), 0, len);
    }

    private void writeZoned(BigDecimal value, int totalDigits, int decimalPlaces) throws IOException {
        String zoned = ZonedDecimalUtil.format(value, totalDigits, decimalPlaces);
        out.write(zoned.getBytes(StandardCharsets.ISO_8859_1));
    }

    private void writePacked(BigDecimal value, int totalDigits, int decimalPlaces) throws IOException {
        byte[] packed = PackedDecimalUtil.encode(value, totalDigits, decimalPlaces);
        out.write(packed);
    }

    private static String padRight(String s, int len) {
        if (s == null) s = "";
        if (s.length() >= len) return s.substring(0, len);
        return s + " ".repeat(len - s.length());
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
