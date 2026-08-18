package com.carddemo.statement;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * End-to-end parity pipeline: generate the Java statements, split the COBOL reference
 * output per account, compare both sides record-by-record and write the frozen-contract
 * {@code parity-summary.json}.
 */
public final class ParityRunner {

    private static final String SOURCE_PROGRAM = "CBSTM03A";

    private final Path repoRoot;

    public ParityRunner(Path repoRoot) {
        this.repoRoot = repoRoot.toAbsolutePath().normalize();
    }

    public Path run() {
        Path asciiDir = repoRoot.resolve("app/data/ASCII");
        Path parityDir = repoRoot.resolve("carddemo-java/target/parity");
        Path javaDir = parityDir.resolve("java");
        Path cobolDir = parityDir.resolve("cobol");
        Path cobolRefDir = repoRoot.resolve("carddemo-java/target/cobol-ref");

        List<AccountStatement> statements = new StatementGenerator(CardDemoData.load(asciiDir)).generate();

        Map<String, List<String>> javaText = new LinkedHashMap<>();
        Map<String, List<String>> javaHtml = new LinkedHashMap<>();
        List<String> allText = new ArrayList<>();
        List<String> allHtml = new ArrayList<>();
        for (AccountStatement statement : statements) {
            javaText.put(statement.accountId(), statement.textRecords());
            javaHtml.put(statement.accountId(), statement.htmlRecords());
            allText.addAll(statement.textRecords());
            allHtml.addAll(statement.htmlRecords());
            RecordFiles.writeRecords(javaDir.resolve(statement.accountId() + ".txt"),
                    statement.textRecords(), AccountStatement.TEXT_RECORD_LENGTH);
            RecordFiles.writeRecords(javaDir.resolve(statement.accountId() + ".html"),
                    statement.htmlRecords(), AccountStatement.HTML_RECORD_LENGTH);
        }
        RecordFiles.writeRecords(javaDir.resolve("statement.txt"), allText,
                AccountStatement.TEXT_RECORD_LENGTH);
        RecordFiles.writeRecords(javaDir.resolve("statement.html"), allHtml,
                AccountStatement.HTML_RECORD_LENGTH);

        Map<String, List<String>> cobolText = new LinkedHashMap<>();
        Map<String, List<String>> cobolHtml = new LinkedHashMap<>();
        Path cobolTextFile = cobolRefDir.resolve("statement.txt");
        Path cobolHtmlFile = cobolRefDir.resolve("statement.html");
        if (Files.exists(cobolTextFile)) {
            cobolText.putAll(CobolOutputSplitter.splitText(RecordFiles.readRecords(
                    cobolTextFile, AccountStatement.TEXT_RECORD_LENGTH)));
        }
        if (Files.exists(cobolHtmlFile)) {
            cobolHtml.putAll(CobolOutputSplitter.splitHtml(RecordFiles.readRecords(
                    cobolHtmlFile, AccountStatement.HTML_RECORD_LENGTH)));
        }
        cobolText.forEach((accountId, records) -> RecordFiles.writeRecords(
                cobolDir.resolve(accountId + ".txt"), records, AccountStatement.TEXT_RECORD_LENGTH));
        cobolHtml.forEach((accountId, records) -> RecordFiles.writeRecords(
                cobolDir.resolve(accountId + ".html"), records, AccountStatement.HTML_RECORD_LENGTH));

        LinkedHashSet<String> accountIds = new LinkedHashSet<>(javaText.keySet());
        accountIds.addAll(cobolText.keySet());
        accountIds.addAll(cobolHtml.keySet());

        Map<String, AccountStatement> byId = new LinkedHashMap<>();
        statements.forEach(statement -> byId.put(statement.accountId(), statement));

        List<String> entries = new ArrayList<>();
        int match = 0;
        int diff = 0;
        int missing = 0;
        for (String accountId : accountIds) {
            AccountStatement statement = byId.get(accountId);
            ParityResult result = ParityResult.compare(cobolText.get(accountId), javaText.get(accountId),
                    cobolHtml.get(accountId), javaHtml.get(accountId));
            switch (result.status()) {
                case ParityResult.MATCH -> match++;
                case ParityResult.DIFF -> diff++;
                default -> missing++;
            }
            entries.add(accountEntry(accountId, statement, result,
                    cobolText.containsKey(accountId), cobolHtml.containsKey(accountId)));
        }

        String json = """
                {
                  "generatedAt": %s,
                  "sourceProgram": %s,
                  "summary": { "total": %d, "match": %d, "diff": %d, "missing": %d },
                  "accounts": [
                %s
                  ]
                }
                """.formatted(quote(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()),
                quote(SOURCE_PROGRAM), accountIds.size(), match, diff, missing,
                String.join(",\n", entries));

        Path summary = parityDir.resolve("parity-summary.json");
        try {
            Files.createDirectories(parityDir);
            Files.writeString(summary, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + summary, e);
        }
        return summary;
    }

    private String accountEntry(String accountId, AccountStatement statement, ParityResult result,
                                boolean hasCobolText, boolean hasCobolHtml) {
        String cobolTextPath = hasCobolText
                ? quote("carddemo-java/target/parity/cobol/" + accountId + ".txt") : "null";
        String cobolHtmlPath = hasCobolHtml
                ? quote("carddemo-java/target/parity/cobol/" + accountId + ".html") : "null";
        String javaTextPath = statement != null
                ? quote("carddemo-java/target/parity/java/" + accountId + ".txt") : "null";
        String javaHtmlPath = statement != null
                ? quote("carddemo-java/target/parity/java/" + accountId + ".html") : "null";
        BigDecimal total = statement != null ? statement.totalAmount() : BigDecimal.ZERO.setScale(2);
        String entry = """
                {
                  "accountId": %s,
                  "customerName": %s,
                  "transactionCount": %d,
                  "totalAmount": %s,
                  "cobolTextPath": %s,
                  "cobolHtmlPath": %s,
                  "javaTextPath": %s,
                  "javaHtmlPath": %s,
                  "status": %s,
                  "diffLineCount": %d,
                  "firstDiffLine": %s
                }"""
                .formatted(quote(accountId),
                        quote(statement != null ? statement.customerName() : ""),
                        statement != null ? statement.transactionCount() : 0,
                        total.toPlainString(),
                        cobolTextPath, cobolHtmlPath, javaTextPath, javaHtmlPath,
                        quote(result.status()), result.diffLineCount(),
                        result.firstDiffLine() == null ? "null" : result.firstDiffLine().toString());
        return entry.indent(4).stripTrailing();
    }

    static String quote(String value) {
        StringBuilder out = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
