package com.carddemo.statement.viewer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatementContractTest {
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    @Test
    void deserializesContractSampleWithoutChangingValues() throws Exception {
        Statement statement =
                objectMapper.readValue(Path.of("docs/statement-contract.json").toFile(), Statement.class);

        assertThat(statement.accountId()).isEqualTo("00000000011");
        assertThat(statement.customer()).isEqualTo(new Customer(
                "John",
                "Doe",
                java.util.List.of("123 Main Street", "Apt 4B", "Springfield IL USA 62701")));
        assertThat(statement.cardNumber()).isEqualTo("****-****-****-1234");
        assertThat(statement.transactions()).containsExactly(
                new Transaction("TX000000000000001", "GROCERY STORE PURCHASE", new BigDecimal("-123.45")));
        assertThat(statement.totalAmount()).isEqualByComparingTo("-456.78");
    }

    @Test
    void rejectsUnknownProperties() {
        assertThatThrownBy(() -> objectMapper.readValue(
                        "{\"accountId\":\"1\",\"customer\":{\"firstName\":\"A\",\"lastName\":\"B\",\"addressLines\":[\"C\"]},"
                                + "\"cardNumber\":\"x\",\"transactions\":[],\"totalAmount\":0,\"unexpected\":true}",
                        Statement.class))
                .isInstanceOf(com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException.class);
    }
}
