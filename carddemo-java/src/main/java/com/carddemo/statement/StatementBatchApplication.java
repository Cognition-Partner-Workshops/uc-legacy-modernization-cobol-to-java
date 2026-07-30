package com.carddemo.statement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class StatementBatchApplication {
    private StatementBatchApplication() {}

    public static void main(String[] args) throws IOException {
        Path repoRoot = Path.of("").toAbsolutePath().normalize();
        Path dataDir = resolve(repoRoot, args.length > 0 ? args[0] : "app/data/ASCII");
        Path outputDir = resolve(repoRoot, args.length > 1 ? args[1] : "carddemo-java/output/statements");
        List<String> accounts = read(dataDir.resolve("acctdata.txt"));
        List<String> customers = read(dataDir.resolve("custdata.txt"));
        List<String> xrefs = read(dataDir.resolve("cardxref.txt"));
        List<String> transactions = read(dataDir.resolve("dailytran.txt"));
        List<Statement> statements = StatementGenerator.generate(xrefs, customers, accounts, transactions);
        int written = new StatementJsonWriter().write(statements, outputDir);
        System.out.println("Records read: accounts=" + accounts.size() + ", customers="
                + customers.size() + ", xrefs=" + xrefs.size() + ", transactions=" + transactions.size());
        System.out.println("Statements written: " + written);
    }

    private static List<String> read(Path path) throws IOException {
        return Files.readAllLines(path);
    }

    private static Path resolve(Path root, String path) {
        Path candidate = Path.of(path);
        if (candidate.isAbsolute()) return candidate;
        if (Files.exists(root.resolve(candidate))) return root.resolve(candidate);
        return root.resolve("..").resolve(candidate).normalize();
    }
}
