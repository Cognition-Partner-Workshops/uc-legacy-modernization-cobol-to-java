package com.carddemo.statement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class StatementJsonWriter {
    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);
    private final DefaultPrettyPrinter prettyPrinter = prettyPrinter();

    public int write(Iterable<Statement> statements, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        Set<String> accounts = new HashSet<>();
        int count = 0;
        for (Statement statement : statements) {
            String account = statement.accountId();
            String suffix = accounts.add(account) ? "" : "-" + lastFour(statement.sourceCardNumber());
            Path path = outputDir.resolve("statement-" + account + suffix + ".json");
            mapper.writer(prettyPrinter).writeValue(path.toFile(), statement);
            count++;
        }
        return count;
    }

    private static String lastFour(String cardNumber) {
        String value = cardNumber.trim();
        if (value.length() < 4) throw new IllegalArgumentException("Card number must have four digits");
        return value.substring(value.length() - 4);
    }

    private static DefaultPrettyPrinter prettyPrinter() {
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentObjectsWith(new DefaultIndenter("  ", "\n"));
        printer.indentArraysWith(new DefaultIndenter("  ", "\n"));
        return printer;
    }
}
