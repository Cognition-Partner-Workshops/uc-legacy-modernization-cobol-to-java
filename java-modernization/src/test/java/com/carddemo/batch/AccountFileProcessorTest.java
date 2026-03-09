package com.carddemo.batch;

import com.carddemo.batch.model.*;
import com.carddemo.batch.model.ArrayAccountRecord.BalanceEntry;
import com.carddemo.batch.util.CobolDataParser;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration and unit tests that verify the Java AccountFileProcessor
 * produces results identical to the COBOL CBACT01C program for a set
 * of sample inputs drawn from the original acctdata.txt.
 */
class AccountFileProcessorTest {

    @TempDir
    Path tempDir;

    // ---- Helper: build an AccountRecord matching line 1 of acctdata.txt ----
    private static AccountRecord account1() {
        return new AccountRecord(
                1L, "Y",
                new BigDecimal("194.00"),
                new BigDecimal("2020.00"),
                new BigDecimal("1020.00"),
                "2014-11-20", "2025-05-20", "2025-05-20",
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                "A000000000", ""
        );
    }

    // ---- Helper: build an AccountRecord matching line 5 of acctdata.txt ----
    private static AccountRecord account5() {
        return new AccountRecord(
                5L, "Y",
                new BigDecimal("345.00"),
                new BigDecimal("3819.00"),
                new BigDecimal("2430.00"),
                "2012-10-03", "2025-03-09", "2025-03-09",
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                "A000000000", ""
        );
    }

    // =========================================================================
    // 1300-POPUL-ACCT-RECORD — OutAccountRecord population
    // =========================================================================

    @Nested
    @DisplayName("populateOutRecord — mirrors COBOL 1300-POPUL-ACCT-RECORD")
    class OutRecordTests {

        @Test
        @DisplayName("converts reissue date from YYYY-MM-DD to YYYYMMDD")
        void convertsReissueDate() {
            OutAccountRecord out = AccountFileProcessor.populateOutRecord(account1());
            assertEquals("20250520", out.acctReissueDate());
        }

        @Test
        @DisplayName("defaults debit to 2525.00 when source is zero")
        void defaultsDebitWhenZero() {
            OutAccountRecord out = AccountFileProcessor.populateOutRecord(account1());
            assertEquals(new BigDecimal("2525.00"), out.acctCurrCycDebit());
        }

        @Test
        @DisplayName("preserves non-zero debit as-is")
        void preservesNonZeroDebit() {
            AccountRecord withDebit = new AccountRecord(
                    99L, "Y",
                    new BigDecimal("500.00"), new BigDecimal("5000.00"),
                    new BigDecimal("2000.00"),
                    "2020-01-01", "2025-12-31", "2025-06-15",
                    new BigDecimal("100.00"),
                    new BigDecimal("350.75"),
                    "12345", "GRP001"
            );
            OutAccountRecord out = AccountFileProcessor.populateOutRecord(withDebit);
            assertEquals(new BigDecimal("350.75"), out.acctCurrCycDebit());
        }

        @Test
        @DisplayName("copies all passthrough fields unchanged")
        void copiesPassthroughFields() {
            OutAccountRecord out = AccountFileProcessor.populateOutRecord(account1());
            assertEquals(1L, out.acctId());
            assertEquals("Y", out.acctActiveStatus());
            assertEquals(new BigDecimal("194.00"), out.acctCurrBal());
            assertEquals(new BigDecimal("2020.00"), out.acctCreditLimit());
            assertEquals(new BigDecimal("1020.00"), out.acctCashCreditLimit());
            assertEquals("2014-11-20", out.acctOpenDate());
            assertEquals("2025-05-20", out.acctExpiraionDate());
            assertEquals(new BigDecimal("0.00"), out.acctCurrCycCredit());
        }

        @Test
        @DisplayName("account 5: reissue date converts correctly")
        void account5ReissueDate() {
            OutAccountRecord out = AccountFileProcessor.populateOutRecord(account5());
            assertEquals("20250309", out.acctReissueDate());
        }
    }

    // =========================================================================
    // 1400-POPUL-ARRAY-RECORD — ArrayAccountRecord population
    // =========================================================================

    @Nested
    @DisplayName("populateArrayRecord — mirrors COBOL 1400-POPUL-ARRAY-RECORD")
    class ArrayRecordTests {

        @Test
        @DisplayName("has exactly 5 balance entries")
        void hasFiveEntries() {
            ArrayAccountRecord arr = AccountFileProcessor.populateArrayRecord(account1());
            assertEquals(5, arr.balanceEntries().size());
        }

        @Test
        @DisplayName("entry 1: actual balance, debit = 1005.00")
        void entry1() {
            ArrayAccountRecord arr = AccountFileProcessor.populateArrayRecord(account1());
            BalanceEntry e = arr.balanceEntries().get(0);
            assertEquals(new BigDecimal("194.00"), e.currBal());
            assertEquals(new BigDecimal("1005.00"), e.currCycDebit());
        }

        @Test
        @DisplayName("entry 2: actual balance, debit = 1525.00")
        void entry2() {
            ArrayAccountRecord arr = AccountFileProcessor.populateArrayRecord(account1());
            BalanceEntry e = arr.balanceEntries().get(1);
            assertEquals(new BigDecimal("194.00"), e.currBal());
            assertEquals(new BigDecimal("1525.00"), e.currCycDebit());
        }

        @Test
        @DisplayName("entry 3: hardcoded balance = -1025.00, debit = -2500.00")
        void entry3() {
            ArrayAccountRecord arr = AccountFileProcessor.populateArrayRecord(account1());
            BalanceEntry e = arr.balanceEntries().get(2);
            assertEquals(new BigDecimal("-1025.00"), e.currBal());
            assertEquals(new BigDecimal("-2500.00"), e.currCycDebit());
        }

        @Test
        @DisplayName("entries 4-5: zeroed (COBOL INITIALIZE)")
        void entries4and5() {
            ArrayAccountRecord arr = AccountFileProcessor.populateArrayRecord(account1());
            for (int i = 3; i < 5; i++) {
                BalanceEntry e = arr.balanceEntries().get(i);
                assertEquals(BigDecimal.ZERO, e.currBal(), "entry " + (i + 1) + " bal");
                assertEquals(BigDecimal.ZERO, e.currCycDebit(), "entry " + (i + 1) + " debit");
            }
        }

        @Test
        @DisplayName("account 5: entry 1 uses account's actual balance")
        void account5Entry1() {
            ArrayAccountRecord arr = AccountFileProcessor.populateArrayRecord(account5());
            assertEquals(new BigDecimal("345.00"), arr.balanceEntries().get(0).currBal());
            assertEquals(new BigDecimal("1005.00"), arr.balanceEntries().get(0).currCycDebit());
        }
    }

    // =========================================================================
    // 1500-POPUL-VBRC-RECORD — VB record population
    // =========================================================================

    @Nested
    @DisplayName("populateVbRecords — mirrors COBOL 1500-POPUL-VBRC-RECORD")
    class VbRecordTests {

        @Test
        @DisplayName("VB1: account ID and active status")
        void vb1Fields() {
            VbRecord1 vb1 = AccountFileProcessor.populateVbRecord1(account1());
            assertEquals(1L, vb1.acctId());
            assertEquals("Y", vb1.acctActiveStatus());
        }

        @Test
        @DisplayName("VB2: account ID, balance, credit limit, reissue year")
        void vb2Fields() {
            VbRecord2 vb2 = AccountFileProcessor.populateVbRecord2(account1());
            assertEquals(1L, vb2.acctId());
            assertEquals(new BigDecimal("194.00"), vb2.acctCurrBal());
            assertEquals(new BigDecimal("2020.00"), vb2.acctCreditLimit());
            assertEquals("2025", vb2.acctReissueYear());
        }

        @Test
        @DisplayName("VB2: account 5 reissue year extracted correctly")
        void vb2Account5ReissueYear() {
            VbRecord2 vb2 = AccountFileProcessor.populateVbRecord2(account5());
            assertEquals("2025", vb2.acctReissueYear());
        }
    }

    // =========================================================================
    // End-to-end integration: process sample file and verify outputs
    // =========================================================================

    @Nested
    @DisplayName("end-to-end integration test")
    class IntegrationTests {

        @Test
        @DisplayName("processes sample acctdata and writes three output files")
        void processesSampleFile() throws IOException {
            // Prepare input file (first 3 records)
            String input = String.join("\n",
                "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000",
                "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{A000000000",
                "00000000003Y00000001470{00000049090{00000005380{2013-08-232024-01-102024-01-1000000000000{00000000000{A000000000"
            );
            Path inFile   = tempDir.resolve("acctdata.txt");
            Path outFile  = tempDir.resolve("outfile.csv");
            Path arryFile = tempDir.resolve("arryfile.csv");
            Path vbrcFile = tempDir.resolve("vbrcfile.csv");

            Files.writeString(inFile, input);

            AccountFileProcessor processor = new AccountFileProcessor(
                    inFile, outFile, arryFile, vbrcFile);
            int count = processor.process();

            assertEquals(3, count, "should process 3 records");

            // Verify OUTFILE
            List<String> outLines = Files.readAllLines(outFile);
            assertEquals(3, outLines.size());
            // Record 1: debit defaulted to 2525.00, reissue date converted
            assertTrue(outLines.get(0).contains("2525.00"),
                    "debit should be defaulted to 2525.00 for acct 1");
            assertTrue(outLines.get(0).contains("20250520"),
                    "reissue date should be YYYYMMDD for acct 1");
            // Record 2
            assertTrue(outLines.get(1).startsWith("2,"),
                    "second line should start with acct ID 2");
            assertTrue(outLines.get(1).contains("20240811"),
                    "reissue date for acct 2");

            // Verify ARRYFILE
            List<String> arryLines = Files.readAllLines(arryFile);
            assertEquals(3, arryLines.size());
            // Each line should contain the hardcoded values
            assertTrue(arryLines.get(0).contains("1005.00"));
            assertTrue(arryLines.get(0).contains("1525.00"));
            assertTrue(arryLines.get(0).contains("-1025.00"));
            assertTrue(arryLines.get(0).contains("-2500.00"));

            // Verify VBRCFILE (2 records per account = 6 lines)
            List<String> vbrcLines = Files.readAllLines(vbrcFile);
            assertEquals(6, vbrcLines.size());
            assertTrue(vbrcLines.get(0).startsWith("VB1,1,"));
            assertTrue(vbrcLines.get(1).startsWith("VB2,1,"));
            assertTrue(vbrcLines.get(1).contains("2025"),
                    "VB2 should contain reissue year");
        }

        @Test
        @DisplayName("processes the full 50-record sample file from acctdata.txt")
        void processesFullSampleFile() throws IOException {
            // Use the actual sample file if available, otherwise create synthetic data
            Path sampleFile = Path.of("src/test/resources/acctdata_sample.txt");
            if (!Files.exists(sampleFile)) {
                sampleFile = tempDir.resolve("full_sample.txt");
                Files.writeString(sampleFile, String.join("\n",
                    "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000",
                    "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{A000000000",
                    "00000000003Y00000001470{00000049090{00000005380{2013-08-232024-01-102024-01-1000000000000{00000000000{A000000000",
                    "00000000004Y00000000400{00000035030{00000027890{2012-11-172023-12-162023-12-1600000000000{00000000000{A000000000",
                    "00000000005Y00000003450{00000038190{00000024300{2012-10-032025-03-092025-03-0900000000000{00000000000{A000000000"
                ));
            }

            Path outFile  = tempDir.resolve("out_full.csv");
            Path arryFile = tempDir.resolve("arry_full.csv");
            Path vbrcFile = tempDir.resolve("vbrc_full.csv");

            AccountFileProcessor processor = new AccountFileProcessor(
                    sampleFile, outFile, arryFile, vbrcFile);
            int count = processor.process();

            assertTrue(count >= 5, "should process at least 5 records");

            // Verify each output file has the expected number of lines
            assertEquals(count, Files.readAllLines(outFile).size());
            assertEquals(count, Files.readAllLines(arryFile).size());
            assertEquals(count * 2, Files.readAllLines(vbrcFile).size());
        }
    }

    // =========================================================================
    // Detailed field-level verification against COBOL expected output
    // =========================================================================

    @Nested
    @DisplayName("field-level COBOL equivalence checks")
    class CobolEquivalenceTests {

        /**
         * Verifies that for each of the first 5 sample accounts, the Java
         * output matches the COBOL output field-by-field.
         */
        @Test
        @DisplayName("all 5 sample accounts produce correct OutAccountRecord values")
        void allSampleAccountsOutRecords() {
            // Expected values derived from COBOL logic applied to acctdata.txt
            record Expected(long id, BigDecimal bal, BigDecimal creditLimit,
                            BigDecimal cashCreditLimit, String reissueYyyymmdd,
                            BigDecimal cycDebit) {}

            List<Expected> expected = List.of(
                new Expected(1, bd("194.00"), bd("2020.00"), bd("1020.00"), "20250520", bd("2525.00")),
                new Expected(2, bd("158.00"), bd("6130.00"), bd("5448.00"), "20240811", bd("2525.00")),
                new Expected(3, bd("147.00"), bd("4909.00"), bd("538.00"),  "20240110", bd("2525.00")),
                new Expected(4, bd("40.00"),  bd("3503.00"), bd("2789.00"), "20231216", bd("2525.00")),
                new Expected(5, bd("345.00"), bd("3819.00"), bd("2430.00"), "20250309", bd("2525.00"))
            );

            String[] lines = {
                "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000",
                "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{A000000000",
                "00000000003Y00000001470{00000049090{00000005380{2013-08-232024-01-102024-01-1000000000000{00000000000{A000000000",
                "00000000004Y00000000400{00000035030{00000027890{2012-11-172023-12-162023-12-1600000000000{00000000000{A000000000",
                "00000000005Y00000003450{00000038190{00000024300{2012-10-032025-03-092025-03-0900000000000{00000000000{A000000000",
            };

            for (int i = 0; i < lines.length; i++) {
                AccountRecord acct = CobolDataParser.parseAccountRecord(lines[i]);
                OutAccountRecord out = AccountFileProcessor.populateOutRecord(acct);
                Expected exp = expected.get(i);

                assertAll("account " + exp.id,
                    () -> assertEquals(exp.id, out.acctId()),
                    () -> assertEquals(exp.bal, out.acctCurrBal()),
                    () -> assertEquals(exp.creditLimit, out.acctCreditLimit()),
                    () -> assertEquals(exp.cashCreditLimit, out.acctCashCreditLimit()),
                    () -> assertEquals(exp.reissueYyyymmdd, out.acctReissueDate()),
                    () -> assertEquals(exp.cycDebit, out.acctCurrCycDebit())
                );
            }
        }

        @Test
        @DisplayName("all 5 sample accounts produce correct VbRecord2 values")
        void allSampleAccountsVb2Records() {
            record Expected(long id, BigDecimal bal, BigDecimal creditLimit, String year) {}

            List<Expected> expected = List.of(
                new Expected(1, bd("194.00"), bd("2020.00"), "2025"),
                new Expected(2, bd("158.00"), bd("6130.00"), "2024"),
                new Expected(3, bd("147.00"), bd("4909.00"), "2024"),
                new Expected(4, bd("40.00"),  bd("3503.00"), "2023"),
                new Expected(5, bd("345.00"), bd("3819.00"), "2025")
            );

            String[] lines = {
                "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000",
                "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{A000000000",
                "00000000003Y00000001470{00000049090{00000005380{2013-08-232024-01-102024-01-1000000000000{00000000000{A000000000",
                "00000000004Y00000000400{00000035030{00000027890{2012-11-172023-12-162023-12-1600000000000{00000000000{A000000000",
                "00000000005Y00000003450{00000038190{00000024300{2012-10-032025-03-092025-03-0900000000000{00000000000{A000000000",
            };

            for (int i = 0; i < lines.length; i++) {
                AccountRecord acct = CobolDataParser.parseAccountRecord(lines[i]);
                VbRecord2 vb2 = AccountFileProcessor.populateVbRecord2(acct);
                Expected exp = expected.get(i);

                assertAll("VB2 for account " + exp.id,
                    () -> assertEquals(exp.id, vb2.acctId()),
                    () -> assertEquals(exp.bal, vb2.acctCurrBal()),
                    () -> assertEquals(exp.creditLimit, vb2.acctCreditLimit()),
                    () -> assertEquals(exp.year, vb2.acctReissueYear())
                );
            }
        }

        private static BigDecimal bd(String val) {
            return new BigDecimal(val);
        }
    }
}
