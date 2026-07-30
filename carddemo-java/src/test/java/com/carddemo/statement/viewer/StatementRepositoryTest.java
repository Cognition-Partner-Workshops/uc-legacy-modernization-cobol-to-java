package com.carddemo.statement.viewer;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StatementRepositoryTest {
    @TempDir
    Path tempDir;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    @Test
    void loadsStatementsFromConfiguredDirectoryAndSortsByAccount() throws Exception {
        Files.writeString(tempDir.resolve("statement-00000000022.json"), fixture("00000000022", "A"));
        Files.writeString(tempDir.resolve("statement-00000000011.json"), fixture("00000000011", "B"));

        var statements = new StatementRepository(objectMapper, tempDir).findAll();

        assertThat(statements).extracting(Statement::accountId)
                .containsExactly("00000000011", "00000000022");
    }

    @Test
    void skipsFilenameAndPayloadAccountMismatches() throws Exception {
        Files.writeString(tempDir.resolve("statement-00000000011.json"), fixture("00000000022", "Mismatch"));

        assertThat(new StatementRepository(objectMapper, tempDir).findAll()).isEmpty();
        assertThat(new StatementRepository(objectMapper, tempDir).findByAccountId("00000000011")).isEmpty();
    }

    @Test
    void fallsBackToBundledMocksWhenDirectoryIsMissing() {
        var statements = new StatementRepository(objectMapper, tempDir.resolve("missing")).findAll();

        assertThat(statements).hasSize(3);
        assertThat(statements).extracting(Statement::accountId)
                .containsExactly("00000000011", "00000000022", "00000000033");
    }

    @Test
    void rejectsUnsafeAccountIds() {
        assertThat(new StatementRepository(objectMapper, tempDir).findByAccountId("../00000000011")).isEmpty();
        assertThat(new StatementRepository(objectMapper, tempDir).findByAccountId("statement-00000000011.json"))
                .isEmpty();
    }

    private String fixture(String accountId, String name) {
        return """
                {
                  "accountId": "%s",
                  "customer": {"firstName": "%s", "lastName": "User", "addressLines": ["Address"]},
                  "cardNumber": "****-****-****-1234",
                  "transactions": [],
                  "totalAmount": 0
                }
                """.formatted(accountId, name);
    }
}
