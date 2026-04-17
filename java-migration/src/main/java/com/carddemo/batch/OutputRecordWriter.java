package com.carddemo.batch;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the three output files produced by CBACT01C:
 * <ol>
 *   <li>OUTFILE  - fixed-length account output records</li>
 *   <li>ARRYFILE - array records with repeated balance/debit groups</li>
 *   <li>VBRCFILE - variable-length records (two record types per account)</li>
 * </ol>
 */
public final class OutputRecordWriter implements AutoCloseable {

    private final OutputStream outStream;
    private final OutputStream arryStream;
    private final OutputStream vbrcStream;

    public OutputRecordWriter(Path outFile, Path arryFile, Path vbrcFile) throws IOException {
        this.outStream = new BufferedOutputStream(Files.newOutputStream(outFile));
        this.arryStream = new BufferedOutputStream(Files.newOutputStream(arryFile));
        this.vbrcStream = new BufferedOutputStream(Files.newOutputStream(vbrcFile));
    }

    /**
     * Write the OUTFILE record for one account.
     *
     * <pre>
     * FD OUT-FILE record layout:
     *   05  OUT-ACCT-ID                PIC 9(11)          11 bytes
     *   05  OUT-ACCT-ACTIVE-STATUS     PIC X(01)           1 byte
     *   05  OUT-ACCT-CURR-BAL          PIC S9(10)V99      12 bytes (zoned)
     *   05  OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99      12 bytes (zoned)
     *   05  OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99      12 bytes (zoned)
     *   05  OUT-ACCT-OPEN-DATE         PIC X(10)          10 bytes
     *   05  OUT-ACCT-EXPIRAION-DATE    PIC X(10)          10 bytes
     *   05  OUT-ACCT-REISSUE-DATE      PIC X(10)          10 bytes
     *   05  OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99      12 bytes (zoned)
     *   05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99 COMP-3  7 bytes (packed)
     *   05  OUT-ACCT-GROUP-ID          PIC X(10)          10 bytes
     *                                              Total: 107 bytes
     * </pre>
     */
    public void writeOutRecord(AccountRecord acct, String formattedReissueDate,
                               BigDecimal cycDebitValue) throws IOException {
        byte[] record = new byte[107];
        int pos = 0;

        // OUT-ACCT-ID: PIC 9(11)
        String acctIdStr = CobolDecimalUtils.formatUnsignedNumeric(acct.acctId(), 11);
        System.arraycopy(acctIdStr.getBytes(), 0, record, pos, 11);
        pos += 11;

        // OUT-ACCT-ACTIVE-STATUS: PIC X(01)
        record[pos] = (byte) acct.acctActiveStatus().charAt(0);
        pos += 1;

        // OUT-ACCT-CURR-BAL: PIC S9(10)V99 (zoned decimal, 12 bytes)
        String currBal = CobolDecimalUtils.formatZonedDecimal(acct.acctCurrBal(), 12, 2);
        System.arraycopy(currBal.getBytes(), 0, record, pos, 12);
        pos += 12;

        // OUT-ACCT-CREDIT-LIMIT: PIC S9(10)V99
        String creditLimit = CobolDecimalUtils.formatZonedDecimal(acct.acctCreditLimit(), 12, 2);
        System.arraycopy(creditLimit.getBytes(), 0, record, pos, 12);
        pos += 12;

        // OUT-ACCT-CASH-CREDIT-LIMIT: PIC S9(10)V99
        String cashCreditLimit = CobolDecimalUtils.formatZonedDecimal(acct.acctCashCreditLimit(), 12, 2);
        System.arraycopy(cashCreditLimit.getBytes(), 0, record, pos, 12);
        pos += 12;

        // OUT-ACCT-OPEN-DATE: PIC X(10)
        String openDate = CobolDecimalUtils.fixedWidth(acct.acctOpenDate(), 10);
        System.arraycopy(openDate.getBytes(), 0, record, pos, 10);
        pos += 10;

        // OUT-ACCT-EXPIRAION-DATE: PIC X(10)
        String expDate = CobolDecimalUtils.fixedWidth(acct.acctExpirationDate(), 10);
        System.arraycopy(expDate.getBytes(), 0, record, pos, 10);
        pos += 10;

        // OUT-ACCT-REISSUE-DATE: PIC X(10) (formatted by DateFormatter)
        String reissueDate = CobolDecimalUtils.fixedWidth(formattedReissueDate, 10);
        System.arraycopy(reissueDate.getBytes(), 0, record, pos, 10);
        pos += 10;

        // OUT-ACCT-CURR-CYC-CREDIT: PIC S9(10)V99
        String cycCredit = CobolDecimalUtils.formatZonedDecimal(acct.acctCurrCycCredit(), 12, 2);
        System.arraycopy(cycCredit.getBytes(), 0, record, pos, 12);
        pos += 12;

        // OUT-ACCT-CURR-CYC-DEBIT: PIC S9(10)V99 COMP-3 (7 bytes packed)
        byte[] packedDebit = CobolDecimalUtils.toComp3(cycDebitValue, 2, 7);
        System.arraycopy(packedDebit, 0, record, pos, 7);
        pos += 7;

        // OUT-ACCT-GROUP-ID: PIC X(10)
        String groupId = CobolDecimalUtils.fixedWidth(acct.acctGroupId(), 10);
        System.arraycopy(groupId.getBytes(), 0, record, pos, 10);

        outStream.write(record);
    }

    /**
     * Write the ARRYFILE record for one account.
     *
     * <pre>
     * FD ARRY-FILE record layout:
     *   05  ARR-ACCT-ID                PIC 9(11)          11 bytes
     *   05  ARR-ACCT-BAL OCCURS 5 TIMES:
     *     10  ARR-ACCT-CURR-BAL        PIC S9(10)V99      12 bytes (zoned)
     *     10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 COMP-3  7 bytes (packed)
     *     (each group = 19 bytes, x5 = 95 bytes)
     *   05  ARR-FILLER                 PIC X(04)           4 bytes
     *                                              Total: 110 bytes
     * </pre>
     *
     * Population logic from 1400-POPUL-ARRAY-RECORD:
     *   - Group 1: bal = ACCT-CURR-BAL, debit = 1005.00
     *   - Group 2: bal = ACCT-CURR-BAL, debit = 1525.00
     *   - Group 3: bal = -1025.00,      debit = -2500.00
     *   - Groups 4-5: initialized to zeros
     */
    public void writeArryRecord(AccountRecord acct) throws IOException {
        byte[] record = new byte[110];
        int pos = 0;

        // ARR-ACCT-ID
        String acctIdStr = CobolDecimalUtils.formatUnsignedNumeric(acct.acctId(), 11);
        System.arraycopy(acctIdStr.getBytes(), 0, record, pos, 11);
        pos += 11;

        // 5 array groups: balance (12 zoned) + debit (7 packed) = 19 each
        BigDecimal[] balances = {
                acct.acctCurrBal(),
                acct.acctCurrBal(),
                new BigDecimal("-1025.00"),
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2)
        };
        BigDecimal[] debits = {
                new BigDecimal("1005.00"),
                new BigDecimal("1525.00"),
                new BigDecimal("-2500.00"),
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2)
        };

        for (int i = 0; i < 5; i++) {
            String bal = CobolDecimalUtils.formatZonedDecimal(balances[i], 12, 2);
            System.arraycopy(bal.getBytes(), 0, record, pos, 12);
            pos += 12;

            byte[] packedDebit = CobolDecimalUtils.toComp3(debits[i], 2, 7);
            System.arraycopy(packedDebit, 0, record, pos, 7);
            pos += 7;
        }

        // ARR-FILLER: 4 bytes of spaces
        record[pos] = (byte) ' ';
        record[pos + 1] = (byte) ' ';
        record[pos + 2] = (byte) ' ';
        record[pos + 3] = (byte) ' ';

        arryStream.write(record);
    }

    /**
     * Write the two VBRCFILE variable-length records for one account.
     *
     * Record 1 (VB1): 12 bytes
     *   05  VB1-ACCT-ID              PIC 9(11)   11 bytes
     *   05  VB1-ACCT-ACTIVE-STATUS   PIC X(01)    1 byte
     *
     * Record 2 (VB2): 39 bytes
     *   05  VB2-ACCT-ID              PIC 9(11)           11 bytes
     *   05  VB2-ACCT-CURR-BAL        PIC S9(10)V99       12 bytes (zoned)
     *   05  VB2-ACCT-CREDIT-LIMIT    PIC S9(10)V99       12 bytes (zoned)
     *   05  VB2-ACCT-REISSUE-YYYY    PIC X(04)            4 bytes
     */
    public void writeVbrcRecords(AccountRecord acct) throws IOException {
        String acctIdStr = CobolDecimalUtils.formatUnsignedNumeric(acct.acctId(), 11);

        // VB1 record - 12 bytes
        byte[] vb1 = new byte[12];
        System.arraycopy(acctIdStr.getBytes(), 0, vb1, 0, 11);
        vb1[11] = (byte) acct.acctActiveStatus().charAt(0);
        vbrcStream.write(vb1);

        // VB2 record - 39 bytes
        byte[] vb2 = new byte[39];
        int pos = 0;
        System.arraycopy(acctIdStr.getBytes(), 0, vb2, pos, 11);
        pos += 11;

        String currBal = CobolDecimalUtils.formatZonedDecimal(acct.acctCurrBal(), 12, 2);
        System.arraycopy(currBal.getBytes(), 0, vb2, pos, 12);
        pos += 12;

        String creditLimit = CobolDecimalUtils.formatZonedDecimal(acct.acctCreditLimit(), 12, 2);
        System.arraycopy(creditLimit.getBytes(), 0, vb2, pos, 12);
        pos += 12;

        // VB2-ACCT-REISSUE-YYYY: first 4 chars of the reissue date (YYYY portion)
        // In COBOL: WS-ACCT-REISSUE-YYYY is set from the reissue date field
        String reissueYyyy = acct.acctReissueDate().substring(0, 4);
        System.arraycopy(reissueYyyy.getBytes(), 0, vb2, pos, 4);

        vbrcStream.write(vb2);
    }

    @Override
    public void close() throws IOException {
        outStream.close();
        arryStream.close();
        vbrcStream.close();
    }
}
