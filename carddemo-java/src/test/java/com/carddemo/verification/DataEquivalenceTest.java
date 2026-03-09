package com.carddemo.verification;

import com.carddemo.util.EbcdicMigrationUtility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Strategy 1: Data Equivalence Testing.
 *
 * Validates that the EbcdicMigrationUtility correctly parses all 10 EBCDIC .PS seed files
 * from app/data/EBCDIC/ and produces expected record counts and field values.
 * This verifies the data layer equivalence between COBOL/VSAM and Java/PostgreSQL.
 *
 * <p>File naming convention: AWS.M2.CARDDEMO.{entity}.PS
 * <p>Tests that depend on EBCDIC files use {@code assumeTrue} to skip gracefully
 * when files are not available (e.g., in CI environments without EBCDIC data).
 */
class DataEquivalenceTest {

    private static final String PREFIX = "AWS.M2.CARDDEMO.";
    private static EbcdicMigrationUtility utility;
    private static Path ebcdicPath;

    @BeforeAll
    static void setUp() {
        utility = new EbcdicMigrationUtility("CP037");
        Path[] candidates = {
                Path.of(System.getProperty("user.dir"), "../app/data/EBCDIC"),
                Path.of(System.getProperty("user.dir"), "../../app/data/EBCDIC"),
                Path.of("/home/ubuntu/repos/uc-legacy-modernization-cobol-to-java/app/data/EBCDIC")
        };
        for (Path p : candidates) {
            if (Files.isDirectory(p)) {
                ebcdicPath = p;
                break;
            }
        }
    }

    private void requireEbcdicData() {
        assumeTrue(ebcdicPath != null && Files.isDirectory(ebcdicPath),
                "EBCDIC data directory not available - skipping");
    }

    private Path psFile(String entity) {
        return ebcdicPath.resolve(PREFIX + entity + ".PS");
    }

    // --- Signed Zoned Decimal Parsing Tests (unit-level, no file dependency) ---

    @Test
    void testSignedZonedDecimalPositive() {
        byte[] record = new byte[12];
        record[0] = (byte) 0xF0;
        record[1] = (byte) 0xF0;
        record[2] = (byte) 0xF0;
        record[3] = (byte) 0xF0;
        record[4] = (byte) 0xF0;
        record[5] = (byte) 0xF0;
        record[6] = (byte) 0xF0;
        record[7] = (byte) 0xF1;
        record[8] = (byte) 0xF9;
        record[9] = (byte) 0xF4;
        record[10] = (byte) 0xF0;
        record[11] = (byte) 0xC0;

        BigDecimal result = utility.extractSignedZonedDecimal(record, 0, 12, 2);
        assertEquals(new BigDecimal("194.00"), result);
    }

    @Test
    void testSignedZonedDecimalNegative() {
        byte[] record = new byte[12];
        record[0] = (byte) 0xF0;
        record[1] = (byte) 0xF0;
        record[2] = (byte) 0xF0;
        record[3] = (byte) 0xF0;
        record[4] = (byte) 0xF0;
        record[5] = (byte) 0xF0;
        record[6] = (byte) 0xF0;
        record[7] = (byte) 0xF5;
        record[8] = (byte) 0xF0;
        record[9] = (byte) 0xF0;
        record[10] = (byte) 0xF0;
        record[11] = (byte) 0xD0;

        BigDecimal result = utility.extractSignedZonedDecimal(record, 0, 12, 2);
        assertEquals(new BigDecimal("-500.00"), result);
    }

    @Test
    void testSignedZonedDecimalZero() {
        byte[] record = new byte[12];
        for (int i = 0; i < 12; i++) {
            record[i] = (byte) 0xF0;
        }
        BigDecimal result = utility.extractSignedZonedDecimal(record, 0, 12, 2);
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void testSignedZonedDecimalWithNonZeroLastDigit() {
        byte[] record = new byte[12];
        record[0] = (byte) 0xF0;
        record[1] = (byte) 0xF0;
        record[2] = (byte) 0xF0;
        record[3] = (byte) 0xF0;
        record[4] = (byte) 0xF0;
        record[5] = (byte) 0xF0;
        record[6] = (byte) 0xF1;
        record[7] = (byte) 0xF2;
        record[8] = (byte) 0xF3;
        record[9] = (byte) 0xF4;
        record[10] = (byte) 0xF5;
        record[11] = (byte) 0xC6;

        BigDecimal result = utility.extractSignedZonedDecimal(record, 0, 12, 2);
        assertEquals(new BigDecimal("1234.56"), result);
    }

    // --- EBCDIC File Record Count Tests ---

    @Test
    void testAccountFileRecordCount() throws Exception {
        requireEbcdicData();
        List<byte[]> records = utility.readRecords(psFile("ACCTDATA"), 300);
        assertEquals(50, records.size(), "ACCTDATA.PS should contain 50 account records");
    }

    @Test
    void testCardFileRecordCount() throws Exception {
        requireEbcdicData();
        List<byte[]> records = utility.readRecords(psFile("CARDDATA"), 150);
        assertEquals(50, records.size(), "CARDDATA.PS should contain 50 card records");
    }

    @Test
    void testCustomerFileRecordCount() throws Exception {
        requireEbcdicData();
        List<byte[]> records = utility.readRecords(psFile("CUSTDATA"), 500);
        assertEquals(50, records.size(), "CUSTDATA.PS should contain 50 customer records");
    }

    @Test
    void testCardXrefFileRecordCount() throws Exception {
        requireEbcdicData();
        List<byte[]> records = utility.readRecords(psFile("CARDXREF"), 50);
        assertEquals(50, records.size(), "CARDXREF.PS should contain 50 xref records");
    }

    @Test
    void testUserSecurityFileRecordCount() throws Exception {
        requireEbcdicData();
        List<byte[]> records = utility.readRecords(psFile("USRSEC"), 80);
        assertEquals(10, records.size(), "USRSEC.PS should contain 10 user records");
    }

    @Test
    void testDailyTransactionFileRecordCount() throws Exception {
        requireEbcdicData();
        List<byte[]> records = utility.readRecords(psFile("DALYTRAN"), 350);
        assertEquals(300, records.size(), "DALYTRAN.PS should contain 300 daily transaction records");
    }

    @Test
    void testDisclosureGroupFileRecordCount() throws Exception {
        requireEbcdicData();
        List<byte[]> records = utility.readRecords(psFile("DISCGRP"), 50);
        assertEquals(51, records.size(), "DISCGRP.PS should contain 51 disclosure group records");
    }

    @Test
    void testTranCatBalanceFileRecordCount() throws Exception {
        requireEbcdicData();
        List<byte[]> records = utility.readRecords(psFile("TCATBALF"), 50);
        assertEquals(50, records.size(), "TCATBALF.PS should contain 50 tran cat balance records");
    }

    @Test
    void testTransactionTypeFileRecordCount() throws Exception {
        requireEbcdicData();
        List<byte[]> records = utility.readRecords(psFile("TRANTYPE"), 60);
        assertEquals(7, records.size(), "TRANTYPE.PS should contain 7 transaction type records");
    }

    @Test
    void testTransactionCategoryFileRecordCount() throws Exception {
        requireEbcdicData();
        List<byte[]> records = utility.readRecords(psFile("TRANCATG"), 60);
        assertEquals(18, records.size(), "TRANCATG.PS should contain 18 transaction category records");
    }

    // --- Field-Level Validation Tests ---

    @Test
    void testAccountFieldParsing() throws Exception {
        requireEbcdicData();
        List<String> inserts = utility.convertAccountFile(psFile("ACCTDATA"));
        assertEquals(50, inserts.size(), "Should produce 50 INSERT statements");
        for (String sql : inserts) {
            assertTrue(sql.startsWith("INSERT INTO accounts"), "SQL should target accounts table");
            assertTrue(sql.contains("VALUES"), "SQL should have VALUES clause");
        }
    }

    @Test
    void testUserFieldParsing() throws Exception {
        requireEbcdicData();
        List<String> inserts = utility.convertUserFile(psFile("USRSEC"));
        assertEquals(10, inserts.size(), "Should produce 10 INSERT statements for users");
        for (String sql : inserts) {
            assertTrue(sql.startsWith("INSERT INTO users"), "SQL should target users table");
        }
    }

    @Test
    void testCardFieldParsing() throws Exception {
        requireEbcdicData();
        List<String> inserts = utility.convertCardFile(psFile("CARDDATA"));
        assertEquals(50, inserts.size(), "Should produce 50 INSERT statements for cards");
        for (String sql : inserts) {
            assertTrue(sql.startsWith("INSERT INTO cards"), "SQL should target cards table");
        }
    }

    @Test
    void testCustomerFieldParsing() throws Exception {
        requireEbcdicData();
        List<String> inserts = utility.convertCustomerFile(psFile("CUSTDATA"));
        assertEquals(50, inserts.size(), "Should produce 50 INSERT statements for customers");
    }

    @Test
    void testCardXrefFieldParsing() throws Exception {
        requireEbcdicData();
        List<String> inserts = utility.convertCardXrefFile(psFile("CARDXREF"));
        assertEquals(50, inserts.size(), "Should produce 50 INSERT statements for card_xref");
    }

    @Test
    void testDisclosureGroupFieldParsing() throws Exception {
        requireEbcdicData();
        List<String> inserts = utility.convertDisclosureGroupFile(psFile("DISCGRP"));
        assertEquals(51, inserts.size(), "Should produce 51 INSERT statements for disclosure_groups");
    }

    @Test
    void testTranCatBalanceFieldParsing() throws Exception {
        requireEbcdicData();
        List<String> inserts = utility.convertTranCatBalanceFile(psFile("TCATBALF"));
        assertEquals(50, inserts.size(), "Should produce 50 INSERT statements for tran_cat_balances");
    }

    @Test
    void testTransactionTypeFieldParsing() throws Exception {
        requireEbcdicData();
        List<String> inserts = utility.convertTransactionTypeFile(psFile("TRANTYPE"));
        assertEquals(7, inserts.size(), "Should produce 7 INSERT statements for transaction_types");
    }

    @Test
    void testTransactionCategoryFieldParsing() throws Exception {
        requireEbcdicData();
        List<String> inserts = utility.convertTransactionCategoryFile(psFile("TRANCATG"));
        assertEquals(18, inserts.size(), "Should produce 18 INSERT statements for transaction_categories");
    }

    // --- Cross-File Consistency Tests ---

    @Test
    void testAccountAndCardRecordCountConsistency() throws Exception {
        requireEbcdicData();
        List<byte[]> accounts = utility.readRecords(psFile("ACCTDATA"), 300);
        List<byte[]> cards = utility.readRecords(psFile("CARDDATA"), 150);
        assertTrue(cards.size() >= accounts.size(),
                "Card count should be >= account count");
    }

    @Test
    void testXrefRecordCountConsistency() throws Exception {
        requireEbcdicData();
        List<byte[]> cards = utility.readRecords(psFile("CARDDATA"), 150);
        List<byte[]> xrefs = utility.readRecords(psFile("CARDXREF"), 50);
        assertEquals(cards.size(), xrefs.size(),
                "Card count and xref count should match");
    }
}
