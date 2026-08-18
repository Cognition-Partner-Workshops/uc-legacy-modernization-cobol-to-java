package com.carddemo.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StatementGeneratorTest {

    /**
     * The full expected text statement for the first account of the sample data,
     * as produced by the COBOL reference run of CBSTM03A.
     */
    private static final List<String> EXPECTED_ACCOUNT_50 = List.of(
            "*******************************START OF STATEMENT*******************************",
            "Aniya Alba Von                                                                  ",
            "1588 Nienow Cape                                                                ",
            "Suite 187                                                                       ",
            "New OR USA 04257                                                                ",
            "--------------------------------------------------------------------------------",
            "                                 Basic Details                                  ",
            "--------------------------------------------------------------------------------",
            "Account ID         :00000000050                                                 ",
            "Current Balance    :000000492.00                                                ",
            "FICO Score         :623                                                         ",
            "--------------------------------------------------------------------------------",
            "                              TRANSACTION SUMMARY                               ",
            "--------------------------------------------------------------------------------",
            "Tran ID         Tran Details                                         Tran Amount",
            "--------------------------------------------------------------------------------",
            "0000000058866561 Purchase at Blick-Rippin                         $      183.88 ",
            "0000000329724245 Purchase at Reichel Group                        $       14.00 ",
            "0000000475746885 Purchase at Adams-Watsica                        $      967.44 ",
            "0000000577826814 Return item at DuBuque, Wuckert and Mraz         $       47.88-",
            "0000000685488982 Purchase at Williamson Group                     $       94.77 ",
            "0000000838587312 Purchase at Abbott-Gerlach                       $      241.66 ",
            "--------------------------------------------------------------------------------",
            "Total EXP:                                                        $     1453.87 ",
            "********************************END OF STATEMENT********************************");

    private static List<AccountStatement> statements;

    @BeforeAll
    static void generate() {
        statements = new StatementGenerator(CardDemoData.load(repoRoot().resolve("app/data/ASCII"))).generate();
    }

    @Test
    void generatesOneStatementPerXrefRecord() {
        assertEquals(50, statements.size());
        assertEquals(300, statements.stream().mapToInt(AccountStatement::transactionCount).sum());
    }

    @Test
    void firstStatementMatchesTheCobolReferenceTextOutput() {
        AccountStatement statement = statements.get(0);
        assertEquals("00000000050", statement.accountId());
        assertEquals("Aniya Alba Von", statement.customerName());
        assertEquals(6, statement.transactionCount());
        assertEquals(new BigDecimal("1453.87"), statement.totalAmount());
        assertEquals(EXPECTED_ACCOUNT_50, statement.textRecords());
    }

    @Test
    void everyTextRecordIs80CharsAndEveryHtmlRecordIs100Chars() {
        for (AccountStatement statement : statements) {
            statement.textRecords().forEach(record ->
                    assertEquals(AccountStatement.TEXT_RECORD_LENGTH, record.length(), record));
            statement.htmlRecords().forEach(record ->
                    assertEquals(AccountStatement.HTML_RECORD_LENGTH, record.length(), record));
        }
    }

    @Test
    void htmlStatementOpensAndClosesTheDocumentAndCarriesTheAccountHeading() {
        List<String> html = statements.get(0).htmlRecords();
        assertEquals(CobolText.alphanumeric("<!DOCTYPE html>", 100), html.get(0));
        assertEquals(CobolText.alphanumeric("</html>", 100), html.get(html.size() - 1));
        assertTrue(html.stream().anyMatch(record ->
                record.startsWith("<h3>Statement for Account Number: 00000000050")));
        assertTrue(html.stream().anyMatch(record ->
                record.startsWith("<p style=\"font-size:16px\">Aniya Alba Von  </p>")));
    }

    static Path repoRoot() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null && !Files.isDirectory(dir.resolve("app/data/ASCII"))) {
            dir = dir.getParent();
        }
        if (dir == null) {
            throw new IllegalStateException("Cannot locate repository root containing app/data/ASCII");
        }
        return dir;
    }
}
