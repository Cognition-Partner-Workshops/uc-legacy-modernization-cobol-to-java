package com.carddemo.parity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParityReportRendererTest {

    private static ParitySummary fixture;
    private static String html;

    @BeforeAll
    static void renderFixture() throws IOException {
        fixture = readFixture();
        html = new ParityReportRenderer().render(fixture);
    }

    private static ParitySummary readFixture() throws IOException {
        try (InputStream in = ParityReportRendererTest.class
                .getResourceAsStream("/parity-summary-fixture.json")) {
            assertTrue(in != null, "fixture JSON must be on the test classpath");
            return new ParitySummaryReader().read(in);
        }
    }

    @Test
    void parsesFixtureAccordingToContract() {
        assertEquals("CBSTM03A", fixture.sourceProgram());
        assertEquals("2026-08-18T15:00:00Z", fixture.generatedAt());
        assertEquals(5, fixture.summary().total());
        assertEquals(2, fixture.summary().match());
        assertEquals(2, fixture.summary().diff());
        assertEquals(1, fixture.summary().missing());
        assertEquals(5, fixture.accounts().size());
        assertEquals(fixture.summary().total(), fixture.accounts().size());
    }

    @Test
    void tolerantOfContractVariation() throws IOException {
        String json = """
                {"accounts":[{"status":"DIFF","firstDiffLine":9,"diffLineCount":2,
                  "totalAmount":12,"transactionCount":1,"customerName":"A B",
                  "accountId":"00000000009","cobolTextPath":null,"cobolHtmlPath":null,
                  "javaTextPath":null,"javaHtmlPath":null}],
                 "summary":{"missing":0,"diff":1,"match":0,"total":1},
                 "sourceProgram":"CBSTM03A","generatedAt":"2026-08-18T15:00:00Z"}
                """;
        ParitySummary summary = new ParitySummaryReader().read(json);
        assertEquals(1, summary.accounts().size());
        AccountParity account = summary.accounts().get(0);
        assertEquals(ParityStatus.DIFF, account.status());
        assertEquals(0, new BigDecimal("12").compareTo(account.totalAmount()));
        assertEquals(9, account.firstDiffLine());

        String out = new ParityReportRenderer().render(summary);
        assertTrue(out.contains("$12.00"), "integer totalAmount should render as currency");
    }

    @Test
    void isSingleCompleteHtmlDocument() {
        assertTrue(html.startsWith("<!DOCTYPE html>"));
        assertEquals(1, countOccurrences(html, "<html"));
        assertEquals(1, countOccurrences(html, "</html>"));
        assertEquals(1, countOccurrences(html, "<body"));
        assertEquals(1, countOccurrences(html, "</body>"));
        assertTrue(html.trim().endsWith("</html>"));
    }

    @Test
    void rendersSummaryHeaderAndTotals() {
        assertTrue(html.contains("2026-08-18T15:00:00Z"), "generatedAt must be shown");
        assertTrue(html.contains("CBSTM03A"), "sourceProgram must be shown");
        assertTrue(html.contains("PARITY GAPS"), "overall verdict must be shown");

        assertEquals("5", cardValue(html, "total"));
        assertEquals("2", cardValue(html, "match"));
        assertEquals("2", cardValue(html, "diff"));
        assertEquals("1", cardValue(html, "missing"));
    }

    @Test
    void fullParityVerdictWhenNoGaps() {
        ParitySummary allMatch = new ParitySummary(
                "2026-08-18T15:00:00Z", "CBSTM03A",
                new ParitySummary.Totals(1, 1, 0, 0),
                List.of(new AccountParity("00000000001", "Solo Account", 1,
                        new BigDecimal("10.00"),
                        "a.txt", "a.html", "b.txt", "b.html",
                        ParityStatus.MATCH, 0, null)));
        String out = new ParityReportRenderer().render(allMatch);
        assertTrue(out.contains("FULL PARITY"));
        assertFalse(out.contains("PARITY GAPS"));
    }

    @Test
    void rendersOneRowPerAccountWithCorrectBadge() {
        List<String> rows = tableRows(html);
        assertEquals(fixture.accounts().size(), rows.size());

        for (int i = 0; i < rows.size(); i++) {
            AccountParity account = fixture.accounts().get(i);
            String row = rows.get(i);
            String expectedBadge = "badge badge-" + account.status().name().toLowerCase();
            assertTrue(row.contains(expectedBadge),
                    "row for " + account.accountId() + " must carry " + expectedBadge);
            assertEquals(1, countOccurrences(row, "class=\"badge "),
                    "exactly one badge per row");
            assertTrue(row.contains(account.accountId()));
            assertTrue(row.contains(ParityReportRenderer.esc(account.customerName())));
            assertTrue(row.contains(">" + account.transactionCount() + "<"));
        }
    }

    @Test
    void rendersCurrencyWithVisibleNegatives() {
        assertTrue(html.contains("$1,234.56"));
        assertTrue(html.contains("$9,087.00"), "integer amounts render with 2 dp");
        assertTrue(html.contains("$15,432.07"));
        assertTrue(html.contains("-$482.19"), "negative amounts must be visibly negative");
        assertTrue(rowFor("00000000002").contains("amount negative"));
    }

    @Test
    void showsDiffDetailsForDiffAndMissingRows() {
        String diffRow = rowFor("00000000002");
        assertTrue(diffRow.contains("Differing lines"));
        assertTrue(diffRow.contains(">3<"), "diffLineCount must be shown");
        assertTrue(diffRow.contains("First diff line"));
        assertTrue(diffRow.contains(">12<"), "firstDiffLine must be shown");

        String bigDiffRow = rowFor("00000000005");
        assertTrue(bigDiffRow.contains(">41<"));
        assertTrue(bigDiffRow.contains(">5<"));

        String missingRow = rowFor("00000000004");
        assertTrue(missingRow.contains("First diff line"));
        assertTrue(missingRow.contains("n/a"), "null firstDiffLine renders as n/a");
        assertTrue(missingRow.contains("absent"), "MISSING rows explain the absent side");

        String matchRow = rowFor("00000000001");
        assertTrue(matchRow.contains("No differences"));
    }

    @Test
    void rendersArtifactLinksAndHandlesNullsGracefully() {
        String matchRow = rowFor("00000000001");
        for (String path : List.of(
                "carddemo-java/target/parity/cobol/00000000001.txt",
                "carddemo-java/target/parity/cobol/00000000001.html",
                "carddemo-java/target/parity/java/00000000001.txt",
                "carddemo-java/target/parity/java/00000000001.html")) {
            assertTrue(matchRow.contains("href=\"../../../" + path + "\""),
                    "expected clickable relative link to " + path);
        }

        String missingRow = rowFor("00000000004");
        assertTrue(missingRow.contains("carddemo-java/target/parity/cobol/00000000004.txt"));
        assertFalse(missingRow.contains("href=\"../../../null"), "null paths must not become links");
        assertFalse(missingRow.contains(">null<"));
        assertEquals(2, countOccurrences(missingRow, "link-missing"),
                "both absent Java artifacts flagged as missing");
    }

    @Test
    void isSelfContained() {
        assertFalse(html.contains("http://"));
        assertFalse(html.contains("https://"));
        assertFalse(html.contains("//cdn"));
        assertFalse(html.contains("<link"));
        assertFalse(html.contains("<script"));
        assertTrue(html.contains("<style>"), "CSS must be inlined");
    }

    @Test
    void rendersEmptyAccountsSummaryGracefully() {
        ParitySummary empty = new ParitySummary(
                "2026-08-18T15:00:00Z", "CBSTM03A",
                new ParitySummary.Totals(0, 0, 0, 0), List.of());
        String out = new ParityReportRenderer().render(empty);

        assertTrue(out.startsWith("<!DOCTYPE html>"));
        assertTrue(out.trim().endsWith("</html>"));
        assertTrue(out.contains("No accounts"));
        assertFalse(out.contains("<tbody"), "no table body without accounts");
        assertEquals("0", cardValue(out, "total"));
        assertTrue(out.contains("NO DATA"));
    }

    @Test
    void rendersHundredsOfAccounts() {
        List<AccountParity> many = new ArrayList<>();
        for (int i = 1; i <= 400; i++) {
            ParityStatus status = ParityStatus.values()[i % 3];
            many.add(new AccountParity(
                    String.format("%011d", i), "Customer " + i, i,
                    new BigDecimal(i + ".05"),
                    status == ParityStatus.MISSING ? null : "cobol/" + i + ".txt",
                    status == ParityStatus.MISSING ? null : "cobol/" + i + ".html",
                    "java/" + i + ".txt", "java/" + i + ".html",
                    status, status == ParityStatus.MATCH ? 0 : i,
                    status == ParityStatus.MATCH ? null : i));
        }
        ParitySummary summary = new ParitySummary("2026-08-18T15:00:00Z", "CBSTM03A",
                new ParitySummary.Totals(400, 134, 133, 133), many);

        String out = new ParityReportRenderer().render(summary);
        assertEquals(400, tableRows(out).size());
        assertTrue(out.trim().endsWith("</html>"));
    }

    @Test
    void escapesHtmlInData() {
        ParitySummary summary = new ParitySummary(
                "2026-08-18T15:00:00Z", "CBSTM03A",
                new ParitySummary.Totals(1, 0, 1, 0),
                List.of(new AccountParity("00000000007", "<script>alert(1)</script>", 1,
                        new BigDecimal("1.00"), "a.txt", "a.html", "b.txt", "b.html",
                        ParityStatus.DIFF, 1, 1)));
        String out = new ParityReportRenderer().render(summary);
        assertFalse(out.contains("<script"));
        assertTrue(out.contains("&lt;script&gt;"));
    }

    @Test
    void mainWritesReportToGivenPath() throws Exception {
        Path dir = Files.createTempDirectory("parity-main");
        Path input = dir.resolve("parity-summary.json");
        try (InputStream in = getClass().getResourceAsStream("/parity-summary-fixture.json")) {
            Files.copy(in, input);
        }
        Path output = dir.resolve("nested/parity-report.html");

        ParityReportRenderer.main(new String[] {input.toString(), output.toString()});

        String written = Files.readString(output, StandardCharsets.UTF_8);
        assertEquals(html, written);
    }

    private static String rowFor(String accountId) {
        return tableRows(html).stream()
                .filter(row -> row.contains(accountId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no row for account " + accountId));
    }

    private static List<String> tableRows(String document) {
        int start = document.indexOf("<tbody>");
        if (start < 0) {
            return List.of();
        }
        String body = document.substring(start, document.indexOf("</tbody>"));
        List<String> rows = new ArrayList<>();
        Matcher matcher = Pattern.compile("<tr class=\"row-.*?</tr>", Pattern.DOTALL).matcher(body);
        while (matcher.find()) {
            rows.add(matcher.group());
        }
        return rows;
    }

    private static String cardValue(String document, String kind) {
        Matcher matcher = Pattern.compile(
                        "card card-" + kind + "\".*?card-value\">(\\d+)<", Pattern.DOTALL)
                .matcher(document);
        assertTrue(matcher.find(), "missing " + kind + " card");
        return matcher.group(1);
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int index = haystack.indexOf(needle);
        while (index >= 0) {
            count++;
            index = haystack.indexOf(needle, index + needle.length());
        }
        return count;
    }
}
