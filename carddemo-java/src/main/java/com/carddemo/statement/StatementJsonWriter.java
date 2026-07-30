package com.carddemo.statement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class StatementJsonWriter {
    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);

    public int write(Iterable<Statement> statements, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        Set<String> accounts = new HashSet<>();
        int count = 0;
        for (Statement statement : statements) {
            String account = statement.accountId();
            String suffix = accounts.add(account) ? "" : "-" + statement.cardNumber().substring(11);
            Path path = outputDir.resolve("statement-" + account + suffix + ".json");
            mapper.writeValue(path.toFile(), statement);
            count++;
        }
        return count;
    }
}
