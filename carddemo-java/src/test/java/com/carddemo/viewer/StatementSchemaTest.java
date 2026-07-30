package com.carddemo.viewer;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class StatementSchemaTest {
    @Test
    void allTestFixturesMatchLockedSchema() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
                .getSchema(mapper.readTree(getClass().getResourceAsStream("/statement.schema.json")));

        try (var fixtures = Files.list(Path.of("src/test/resources/fixtures"))) {
            fixtures.filter(path -> path.getFileName().toString().endsWith(".json")).forEach(path -> {
                try {
                    JsonNode document = mapper.readTree(path.toFile());
                    assertThat(schema.validate(document))
                            .as("schema violations in %s", path)
                            .isEmpty();
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        }
    }
}
