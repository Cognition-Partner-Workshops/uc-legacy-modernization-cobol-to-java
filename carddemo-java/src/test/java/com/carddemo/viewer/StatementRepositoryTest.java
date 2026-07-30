package com.carddemo.viewer;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StatementRepositoryTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void loadsAndSortsByAccountThenCard(@TempDir Path directory) throws IOException {
        write(directory, "statement-z.json", statement("00000000022", "**-**-****-9000"));
        write(directory, "statement-a.json", statement("00000000011", "**-**-****-9999"));
        write(directory, "statement-b.json", statement("00000000011", "**-**-****-1111"));
        write(directory, "not-a-statement.json", statement("00000000001", "**-**-****-0001"));

        StatementRepository repository = new StatementRepository(objectMapper, directory.toString());

        assertThat(repository.findAll()).extracting(Statement::accountId, Statement::cardNumber)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("00000000011", "**-**-****-1111"),
                        org.assertj.core.groups.Tuple.tuple("00000000011", "**-**-****-9999"),
                        org.assertj.core.groups.Tuple.tuple("00000000022", "**-**-****-9000"));
        assertThat(repository.findByAccountId("00000000011").get().cardNumber())
                .isEqualTo("**-**-****-1111");
    }

    @Test
    void missingDirectoryReturnsEmpty(@TempDir Path directory) {
        StatementRepository repository = new StatementRepository(objectMapper, directory.resolve("missing").toString());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void emptyDirectoryReturnsEmpty(@TempDir Path directory) {
        StatementRepository repository = new StatementRepository(objectMapper, directory.toString());

        assertThat(repository.findAll()).isEmpty();
    }

    private void write(Path directory, String filename, String json) throws IOException {
        Files.writeString(directory.resolve(filename), json);
    }

    private String statement(String accountId, String cardNumber) throws IOException {
        return objectMapper.writeValueAsString(new Statement(
                accountId,
                new Statement.Customer("Test", "User", java.util.List.of("one", "two", "three")),
                cardNumber,
                new java.math.BigDecimal("1.00"),
                700,
                java.util.List.of(),
                java.math.BigDecimal.ZERO));
    }
}
