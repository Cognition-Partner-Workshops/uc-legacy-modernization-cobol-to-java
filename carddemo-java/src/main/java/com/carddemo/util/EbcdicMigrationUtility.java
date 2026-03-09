package com.carddemo.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for migrating EBCDIC-encoded flat files (.PS datasets) to SQL INSERT statements.
 *
 * <p>The CardDemo sample data resides in app/data/EBCDIC/ as fixed-length,
 * EBCDIC-encoded Physical Sequential (PS) files:
 * <ul>
 *   <li>ACCTDATA.PS  — Account records (300 bytes each, layout from CVACT01Y.cpy)</li>
 *   <li>CARDDATA.PS  — Card records (150 bytes each, layout from CVACT02Y.cpy)</li>
 *   <li>CUSTDATA.PS  — Customer records (500 bytes each, layout from CVCUS01Y.cpy)</li>
 *   <li>CARDXREF.PS  — Card-Account cross-reference (50 bytes each, layout from CVACT03Y.cpy)</li>
 *   <li>USRSEC.PS    — User security records (80 bytes each, layout from CSUSR01Y.cpy)</li>
 *   <li>DALYTRAN.PS  — Daily transactions (350 bytes each, layout from CVTRA06Y.cpy)</li>
 * </ul>
 *
 * <p>Each file uses IBM EBCDIC codepage CP037 (US/Canada) or CP1047 (Latin-1).
 * Numeric fields use zoned decimal (PIC 9) or packed decimal (PIC S9 COMP-3).
 *
 * <p>Usage:
 * <pre>
 *   EbcdicMigrationUtility util = new EbcdicMigrationUtility("CP037");
 *   List&lt;String&gt; inserts = util.convertAccountFile(Path.of("app/data/EBCDIC/ACCTDATA.PS"));
 *   // Write inserts to a .sql file or execute directly
 * </pre>
 */
public class EbcdicMigrationUtility {

    private final Charset ebcdicCharset;

    /**
     * Create utility with specified EBCDIC codepage.
     *
     * @param codepage EBCDIC codepage name (e.g., "CP037", "CP1047", "IBM037")
     */
    public EbcdicMigrationUtility(String codepage) {
        this.ebcdicCharset = Charset.forName(codepage);
    }

    /**
     * Read a fixed-length EBCDIC file and split into records.
     *
     * @param filePath path to the .PS file
     * @param recordLength fixed record length in bytes
     * @return list of raw byte arrays, one per record
     */
    public List<byte[]> readRecords(Path filePath, int recordLength) throws IOException {
        List<byte[]> records = new ArrayList<>();
        byte[] fileBytes = Files.readAllBytes(filePath);

        for (int offset = 0; offset + recordLength <= fileBytes.length; offset += recordLength) {
            byte[] record = new byte[recordLength];
            System.arraycopy(fileBytes, offset, record, 0, recordLength);
            records.add(record);
        }

        return records;
    }

    /**
     * Extract a PIC X (alphanumeric) field from an EBCDIC record.
     * Converts EBCDIC bytes to ASCII string and trims trailing spaces.
     *
     * @param record raw EBCDIC record bytes
     * @param offset 0-based byte offset within record
     * @param length field length in bytes
     * @return trimmed ASCII string
     */
    public String extractAlphanumeric(byte[] record, int offset, int length) {
        byte[] fieldBytes = new byte[length];
        System.arraycopy(record, offset, fieldBytes, 0, length);
        return new String(fieldBytes, ebcdicCharset).trim();
    }

    /**
     * Extract a PIC 9 (zoned decimal / display numeric) field.
     * EBCDIC zoned decimal: each digit is one byte, zones F0-F9 map to 0-9.
     *
     * @param record raw EBCDIC record bytes
     * @param offset 0-based byte offset
     * @param length field length in bytes (number of digits)
     * @return numeric value as long
     */
    public long extractZonedDecimal(byte[] record, int offset, int length) {
        String numStr = extractAlphanumeric(record, offset, length);
        if (numStr.isEmpty()) {
            return 0;
        }
        return Long.parseLong(numStr);
    }

    /**
     * Extract a PIC S9(n)V9(m) COMP-3 (packed decimal) field.
     *
     * <p>Packed decimal format:
     * - Each byte holds two digits (high nibble, low nibble)
     * - Last nibble is sign: C/A/F = positive, D/B = negative
     * - Example: +12345 stored as 01 23 45 0C (4 bytes)
     *
     * @param record raw EBCDIC record bytes
     * @param offset 0-based byte offset
     * @param length field length in bytes
     * @param scale number of implied decimal places (V99 = scale 2)
     * @return BigDecimal value with proper scale
     */
    public BigDecimal extractPackedDecimal(byte[] record, int offset, int length, int scale) {
        byte[] fieldBytes = new byte[length];
        System.arraycopy(record, offset, fieldBytes, 0, length);

        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < length - 1; i++) {
            int b = fieldBytes[i] & 0xFF;
            digits.append(b >> 4);
            digits.append(b & 0x0F);
        }

        // Last byte: high nibble is last digit, low nibble is sign
        int lastByte = fieldBytes[length - 1] & 0xFF;
        digits.append(lastByte >> 4);
        int sign = lastByte & 0x0F;

        boolean negative = (sign == 0x0D || sign == 0x0B);
        String numStr = digits.toString();

        // Remove leading zeros but keep at least one digit
        numStr = numStr.replaceFirst("^0+(?=.)", "");

        BigDecimal value = new BigDecimal(numStr);
        if (scale > 0) {
            value = value.movePointLeft(scale);
        }
        if (negative) {
            value = value.negate();
        }

        return value;
    }

    /**
     * Convert account records from ACCTDATA.PS to SQL INSERT statements.
     * Record layout from CVACT01Y.cpy (300 bytes per record):
     * <pre>
     *   05 ACCT-ID              PIC 9(11)    offset 0,  len 11
     *   05 ACCT-ACTIVE-STATUS   PIC X(01)    offset 11, len 1
     *   05 ACCT-CURR-BAL        PIC S9(10)V99 COMP-3  offset 12, len 7
     *   05 ACCT-CREDIT-LIMIT    PIC S9(10)V99 COMP-3  offset 19, len 7
     *   05 ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99 COMP-3 offset 26, len 7
     *   05 ACCT-OPEN-DATE       PIC X(10)    offset 33, len 10
     *   05 ACCT-EXPIRAION-DATE  PIC X(10)    offset 43, len 10
     *   05 ACCT-REISSUE-DATE    PIC X(10)    offset 53, len 10
     *   05 ACCT-CURR-CYC-CREDIT PIC S9(10)V99 COMP-3 offset 63, len 7
     *   05 ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 COMP-3 offset 70, len 7
     *   05 ACCT-ADDR-ZIP        PIC X(10)    offset 77, len 10
     *   05 ACCT-GROUP-ID        PIC X(10)    offset 87, len 10
     *   05 FILLER               PIC X(193)   offset 97, len 193 (padding)
     * </pre>
     */
    public List<String> convertAccountFile(Path filePath) throws IOException {
        List<String> inserts = new ArrayList<>();
        List<byte[]> records = readRecords(filePath, 300);

        for (byte[] record : records) {
            long acctId = extractZonedDecimal(record, 0, 11);
            String status = extractAlphanumeric(record, 11, 1);
            BigDecimal currBal = extractPackedDecimal(record, 12, 7, 2);
            BigDecimal creditLimit = extractPackedDecimal(record, 19, 7, 2);
            BigDecimal cashCreditLimit = extractPackedDecimal(record, 26, 7, 2);
            String openDate = extractAlphanumeric(record, 33, 10);
            String expDate = extractAlphanumeric(record, 43, 10);
            String reissueDate = extractAlphanumeric(record, 53, 10);
            BigDecimal cycCredit = extractPackedDecimal(record, 63, 7, 2);
            BigDecimal cycDebit = extractPackedDecimal(record, 70, 7, 2);
            String zip = extractAlphanumeric(record, 77, 10);
            String groupId = extractAlphanumeric(record, 87, 10);

            String sql = String.format(
                    "INSERT INTO accounts (acct_id, acct_active_status, acct_curr_bal, "
                            + "acct_credit_limit, acct_cash_credit_limit, acct_open_date, "
                            + "acct_expiration_date, acct_reissue_date, acct_curr_cyc_credit, "
                            + "acct_curr_cyc_debit, acct_addr_zip, acct_group_id) "
                            + "VALUES (%d, '%s', %s, %s, %s, '%s', '%s', '%s', %s, %s, '%s', '%s');",
                    acctId, status, currBal, creditLimit, cashCreditLimit,
                    openDate, expDate, reissueDate, cycCredit, cycDebit, zip, groupId
            );
            inserts.add(sql);
        }

        return inserts;
    }

    /**
     * Convert user security records from USRSEC.PS to SQL INSERT statements.
     * Record layout from CSUSR01Y.cpy (80 bytes per record):
     * <pre>
     *   05 SEC-USR-ID     PIC X(08)  offset 0,  len 8
     *   05 SEC-USR-FNAME  PIC X(20)  offset 8,  len 20
     *   05 SEC-USR-LNAME  PIC X(20)  offset 28, len 20
     *   05 SEC-USR-PWD    PIC X(08)  offset 48, len 8
     *   05 SEC-USR-TYPE   PIC X(01)  offset 56, len 1
     *   05 FILLER         PIC X(23)  offset 57, len 23
     * </pre>
     *
     * <p>Note: Passwords from VSAM are plain-text. They should be BCrypt-hashed
     * before inserting into the modernized database.
     */
    public List<String> convertUserFile(Path filePath) throws IOException {
        List<String> inserts = new ArrayList<>();
        List<byte[]> records = readRecords(filePath, 80);

        for (byte[] record : records) {
            String usrId = extractAlphanumeric(record, 0, 8);
            String firstName = extractAlphanumeric(record, 8, 20);
            String lastName = extractAlphanumeric(record, 28, 20);
            String password = extractAlphanumeric(record, 48, 8);
            String userType = extractAlphanumeric(record, 56, 1);

            // Note: password should be BCrypt-hashed before production use
            String sql = String.format(
                    "INSERT INTO users (usr_id, usr_first_name, usr_last_name, usr_pwd, usr_type) "
                            + "VALUES ('%s', '%s', '%s', '%s', '%s');",
                    usrId, firstName.replace("'", "''"),
                    lastName.replace("'", "''"), password, userType
            );
            inserts.add(sql);
        }

        return inserts;
    }

    /**
     * Escape single quotes in SQL string values.
     */
    private String escapeSql(String value) {
        return value.replace("'", "''");
    }
}
