package com.carddemo.util;

import java.io.IOException;
import java.math.BigDecimal;
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
     * Extract a PIC S9(n)V9(m) field using EBCDIC zoned decimal with sign overpunch.
     *
     * <p>In EBCDIC zoned decimal, each digit occupies one byte (0xF0-0xF9 = '0'-'9').
     * For signed fields, the sign is in the zone nibble of the LAST byte:
     * 0xC0-0xC9 = positive (zone C), 0xD0-0xD9 = negative (zone D),
     * 0xF0-0xF9 = unsigned (zone F).
     */
    public BigDecimal extractSignedZonedDecimal(byte[] record, int offset, int length, int scale) {
        byte[] fieldBytes = new byte[length];
        System.arraycopy(record, offset, fieldBytes, 0, length);

        StringBuilder digits = new StringBuilder();
        boolean negative = false;

        for (int i = 0; i < length; i++) {
            int b = fieldBytes[i] & 0xFF;
            int zoneNibble = (b >> 4) & 0x0F;
            int digitNibble = b & 0x0F;

            if (i == length - 1) {
                if (zoneNibble == 0x0D) {
                    negative = true;
                }
            }

            digits.append(digitNibble);
        }

        String numStr = digits.toString();
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
     * Record layout from CVACT01Y.cpy (300 bytes per record).
     * Uses signed zoned decimal (NOT packed decimal) for monetary fields.
     */
    public List<String> convertAccountFile(Path filePath) throws IOException {
        List<String> inserts = new ArrayList<>();
        List<byte[]> records = readRecords(filePath, 300);

        for (byte[] record : records) {
            long acctId = extractZonedDecimal(record, 0, 11);
            String status = extractAlphanumeric(record, 11, 1);
            BigDecimal currBal = extractSignedZonedDecimal(record, 12, 12, 2);
            BigDecimal creditLimit = extractSignedZonedDecimal(record, 24, 12, 2);
            BigDecimal cashCreditLimit = extractSignedZonedDecimal(record, 36, 12, 2);
            String openDate = extractAlphanumeric(record, 48, 10);
            String expDate = extractAlphanumeric(record, 58, 10);
            String reissueDate = extractAlphanumeric(record, 68, 10);
            BigDecimal cycCredit = extractSignedZonedDecimal(record, 78, 12, 2);
            BigDecimal cycDebit = extractSignedZonedDecimal(record, 90, 12, 2);
            String zip = extractAlphanumeric(record, 102, 10);
            String groupId = extractAlphanumeric(record, 112, 10);

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
     * Convert card records from CARDDATA.PS (150 bytes per record, CVACT02Y.cpy).
     */
    public List<String> convertCardFile(Path filePath) throws IOException {
        List<String> inserts = new ArrayList<>();
        List<byte[]> records = readRecords(filePath, 150);

        for (byte[] record : records) {
            String cardNum = extractAlphanumeric(record, 0, 16);
            long acctId = extractZonedDecimal(record, 16, 11);
            int cvv = (int) extractZonedDecimal(record, 27, 3);
            String embossedName = extractAlphanumeric(record, 30, 50);
            String expDate = extractAlphanumeric(record, 80, 10);
            String status = extractAlphanumeric(record, 90, 1);

            String sql = String.format(
                    "INSERT INTO cards (card_num, card_acct_id, card_cvv_cd, "
                            + "card_embossed_name, card_expiration_date, card_active_status) "
                            + "VALUES ('%s', %d, %d, '%s', '%s', '%s');",
                    cardNum, acctId, cvv, escapeSql(embossedName), expDate, status
            );
            inserts.add(sql);
        }

        return inserts;
    }

    /**
     * Convert customer records from CUSTDATA.PS (500 bytes per record, CVCUS01Y.cpy).
     */
    public List<String> convertCustomerFile(Path filePath) throws IOException {
        List<String> inserts = new ArrayList<>();
        List<byte[]> records = readRecords(filePath, 500);

        for (byte[] record : records) {
            long custId = extractZonedDecimal(record, 0, 9);
            String firstName = extractAlphanumeric(record, 9, 25);
            String middleName = extractAlphanumeric(record, 34, 25);
            String lastName = extractAlphanumeric(record, 59, 25);
            String addr1 = extractAlphanumeric(record, 84, 50);
            String addr2 = extractAlphanumeric(record, 134, 50);
            String addr3 = extractAlphanumeric(record, 184, 50);
            String stateCd = extractAlphanumeric(record, 234, 2);
            String countryCd = extractAlphanumeric(record, 236, 3);
            String zip = extractAlphanumeric(record, 239, 10);
            String phone1 = extractAlphanumeric(record, 249, 15);
            String phone2 = extractAlphanumeric(record, 264, 15);
            long ssn = extractZonedDecimal(record, 279, 9);
            String govtId = extractAlphanumeric(record, 288, 20);
            String dob = extractAlphanumeric(record, 308, 10);
            String eftAcctId = extractAlphanumeric(record, 318, 10);
            String priCardHolder = extractAlphanumeric(record, 328, 1);
            int ficoScore = (int) extractZonedDecimal(record, 329, 3);

            String sql = String.format(
                    "INSERT INTO customers (cust_id, cust_first_name, cust_middle_name, cust_last_name, "
                            + "cust_addr_line_1, cust_addr_line_2, cust_addr_line_3, "
                            + "cust_addr_state_cd, cust_addr_country_cd, cust_addr_zip, "
                            + "cust_phone_num_1, cust_phone_num_2, cust_ssn, cust_govt_issued_id, "
                            + "cust_dob_yyyy_mm_dd, cust_eft_account_id, cust_pri_card_holder_ind, "
                            + "cust_fico_credit_score) "
                            + "VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', "
                            + "'%s', '%s', %d, '%s', '%s', '%s', '%s', %d);",
                    custId, escapeSql(firstName), escapeSql(middleName), escapeSql(lastName),
                    escapeSql(addr1), escapeSql(addr2), escapeSql(addr3),
                    stateCd, countryCd, zip, phone1, phone2, ssn,
                    escapeSql(govtId), dob, eftAcctId, priCardHolder, ficoScore
            );
            inserts.add(sql);
        }

        return inserts;
    }

    /**
     * Convert card cross-reference records from CARDXREF.PS (50 bytes, CVACT03Y.cpy).
     */
    public List<String> convertCardXrefFile(Path filePath) throws IOException {
        List<String> inserts = new ArrayList<>();
        List<byte[]> records = readRecords(filePath, 50);

        for (byte[] record : records) {
            String cardNum = extractAlphanumeric(record, 0, 16);
            long custId = extractZonedDecimal(record, 16, 9);
            long acctId = extractZonedDecimal(record, 25, 11);

            String sql = String.format(
                    "INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id) "
                            + "VALUES ('%s', %d, %d);",
                    cardNum, custId, acctId
            );
            inserts.add(sql);
        }

        return inserts;
    }

    /**
     * Convert daily transaction records from DALYTRAN.PS (350 bytes, CVTRA06Y.cpy).
     */
    public List<String> convertDailyTransactionFile(Path filePath) throws IOException {
        List<String> inserts = new ArrayList<>();
        List<byte[]> records = readRecords(filePath, 350);

        for (byte[] record : records) {
            String tranId = extractAlphanumeric(record, 0, 16);
            String typeCd = extractAlphanumeric(record, 16, 2);
            int catCd = (int) extractZonedDecimal(record, 18, 4);
            String source = extractAlphanumeric(record, 22, 10);
            String desc = extractAlphanumeric(record, 32, 100);
            BigDecimal amt = extractSignedZonedDecimal(record, 132, 11, 2);
            long merchantId = extractZonedDecimal(record, 143, 9);
            String merchantName = extractAlphanumeric(record, 152, 50);
            String merchantCity = extractAlphanumeric(record, 202, 50);
            String merchantZip = extractAlphanumeric(record, 252, 10);
            String cardNum = extractAlphanumeric(record, 262, 16);
            String origTs = extractAlphanumeric(record, 278, 26);
            String procTs = extractAlphanumeric(record, 304, 26);

            String sql = String.format(
                    "INSERT INTO daily_transactions (dalytran_id, dalytran_type_cd, dalytran_cat_cd, "
                            + "dalytran_source, dalytran_desc, dalytran_amt, dalytran_merchant_id, "
                            + "dalytran_merchant_name, dalytran_merchant_city, dalytran_merchant_zip, "
                            + "dalytran_card_num, dalytran_orig_ts, dalytran_proc_ts, processed) "
                            + "VALUES ('%s', '%s', %d, '%s', '%s', %s, %d, '%s', '%s', '%s', '%s', '%s', '%s', false);",
                    tranId, typeCd, catCd, source, escapeSql(desc), amt, merchantId,
                    escapeSql(merchantName), escapeSql(merchantCity), merchantZip,
                    cardNum, origTs, procTs
            );
            inserts.add(sql);
        }

        return inserts;
    }

    /**
     * Convert tran category balance records from TCATBALF.PS (50 bytes, CVTRA01Y.cpy).
     */
    public List<String> convertTranCatBalanceFile(Path filePath) throws IOException {
        List<String> inserts = new ArrayList<>();
        List<byte[]> records = readRecords(filePath, 50);

        for (byte[] record : records) {
            long acctId = extractZonedDecimal(record, 0, 11);
            String typeCd = extractAlphanumeric(record, 11, 2);
            int catCd = (int) extractZonedDecimal(record, 13, 4);
            BigDecimal balance = extractSignedZonedDecimal(record, 17, 11, 2);

            String sql = String.format(
                    "INSERT INTO tran_cat_balances (trancat_acct_id, trancat_type_cd, trancat_cd, trancat_bal) "
                            + "VALUES (%d, '%s', %d, %s);",
                    acctId, typeCd, catCd, balance
            );
            inserts.add(sql);
        }

        return inserts;
    }

    /**
     * Convert disclosure group records from DISCGRP.PS (50 bytes, CVTRA02Y.cpy).
     */
    public List<String> convertDisclosureGroupFile(Path filePath) throws IOException {
        List<String> inserts = new ArrayList<>();
        List<byte[]> records = readRecords(filePath, 50);

        for (byte[] record : records) {
            String groupId = extractAlphanumeric(record, 0, 10);
            String typeCd = extractAlphanumeric(record, 10, 2);
            int catCd = (int) extractZonedDecimal(record, 12, 4);
            BigDecimal intRate = extractSignedZonedDecimal(record, 16, 6, 2);

            String sql = String.format(
                    "INSERT INTO disclosure_groups (dis_acct_group_id, dis_tran_type_cd, "
                            + "dis_tran_cat_cd, dis_int_rate) "
                            + "VALUES ('%s', '%s', %d, %s);",
                    groupId, typeCd, catCd, intRate
            );
            inserts.add(sql);
        }

        return inserts;
    }

    /**
     * Convert transaction type records from TRANTYPE.PS (60 bytes, CVTRA03Y.cpy).
     */
    public List<String> convertTransactionTypeFile(Path filePath) throws IOException {
        List<String> inserts = new ArrayList<>();
        List<byte[]> records = readRecords(filePath, 60);

        for (byte[] record : records) {
            String typeCd = extractAlphanumeric(record, 0, 2);
            String desc = extractAlphanumeric(record, 2, 50);

            String sql = String.format(
                    "INSERT INTO transaction_types (tran_type_cd, tran_type_desc) "
                            + "VALUES ('%s', '%s');",
                    typeCd, escapeSql(desc)
            );
            inserts.add(sql);
        }

        return inserts;
    }

    /**
     * Convert transaction category records from TRANCATG.PS (60 bytes, CVTRA04Y.cpy).
     */
    public List<String> convertTransactionCategoryFile(Path filePath) throws IOException {
        List<String> inserts = new ArrayList<>();
        List<byte[]> records = readRecords(filePath, 60);

        for (byte[] record : records) {
            String typeCd = extractAlphanumeric(record, 0, 2);
            int catCd = (int) extractZonedDecimal(record, 2, 4);
            String desc = extractAlphanumeric(record, 6, 50);

            String sql = String.format(
                    "INSERT INTO transaction_categories (tran_cat_cd, tran_type_cd, tran_cat_desc) "
                            + "VALUES (%d, '%s', '%s');",
                    catCd, typeCd, escapeSql(desc)
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
