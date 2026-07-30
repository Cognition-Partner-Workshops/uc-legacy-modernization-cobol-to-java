package com.carddemo.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StatementBackendTest {
    @Test
    void parsesOverpunchAndUnsignedValues() {
        assertEquals(new BigDecimal("504.77"), FixedWidth.amount("0000005047G"));
        assertEquals(new BigDecimal("-919.00"), FixedWidth.amount("0000009190}"));
        assertEquals(new BigDecimal("0.00"), FixedWidth.amount("0000000000}"));
        assertEquals(new BigDecimal("12.34"), FixedWidth.amount("000000001234"));
        assertEquals(new BigDecimal("-12.34"), FixedWidth.amount("-000000001234"));
    }

    @Test
    void parsesAllFixedWidthLayouts() {
        assertEquals("00000000001", RecordParsers.account(
                "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000").id());
        String customer = "000000001Immanuel                 Madeline                 Kessler                  "
                + "618 Deshaun Route                                 Apt. 802                                          "
                + "Altenwerthshire                                   NCUSA12546     (908)119-8310  (373)693-8684  "
                + "020973888000000000000493684371961-06-080053581756Y274";
        assertEquals("Kessler", RecordParsers.customer(customer).lastName());
        assertEquals("00000000011", RecordParsers.xref("050002445376574000000001000000000011").accountId());
        String transaction = "0000000000683580010001POS TERM  Purchase at Abshire-Lowe"
                + "                                                                            0000005047G800000000"
                + "Abshire-Lowe                                      North Enoshaven                                   72112     "
                + "48594526128770652022-06-10 19:27:53.000000                                              ";
        assertEquals("0000000000683580", RecordParsers.transaction(transaction).transactionId());
    }

    @Test
    void groupsNonConsecutiveCardsAndComputesEmptyTotals() {
        List<String> txns = List.of(transaction("CARD00000000001", "TX1", "0000000001{"),
                transaction("CARD00000000002", "TX2", "0000000002{"),
                transaction("CARD00000000001", "TX3", "0000000003{"));
        Map<String, List<Transaction>> grouped = TransactionGrouper.group(txns);
        assertEquals(List.of("TX1", "TX3"), grouped.get("CARD00000000001").stream()
                .map(Transaction::transactionId).toList());
        assertEquals(new BigDecimal("0.00"), StatementGenerator.generate(
                List.of("CARD000000000003000000001000000000011"),
                List.of(customerLine("000000001")), List.of(accountLine("00000000001")),
                List.of()).get(0).totalAmount());
    }

    @Test
    void masksCardAndAssemblesAddress() {
        assertEquals("**-**-****-1234", StatementGenerator.maskCard("1234567890121234"));
        Statement statement = StatementGenerator.generate(
                List.of("123456789012123400000000100000000001"),
                List.of(customerLine("000000001")), List.of(accountLine("00000000001")),
                List.of()).get(0);
        assertEquals(List.of("First Street", "", "North Enoshaven NY USA 12345"),
                statement.customer().addressLines());
    }

    @Test
    void realDataGeneratesFiftySchemaValidStatements() throws Exception {
        Path root = Path.of("").toAbsolutePath().normalize();
        if (!Files.exists(root.resolve("app/data/ASCII"))) root = root.getParent();
        Path data = root.resolve("app/data/ASCII");
        List<Statement> statements = StatementGenerator.generate(
                Files.readAllLines(data.resolve("cardxref.txt")),
                Files.readAllLines(data.resolve("custdata.txt")),
                Files.readAllLines(data.resolve("acctdata.txt")),
                Files.readAllLines(data.resolve("dailytran.txt")));
        assertEquals(50, statements.size());
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
                .getSchema(getClass().getResourceAsStream("/statement.schema.json"));
        ObjectMapper mapper = new ObjectMapper();
        for (Statement statement : statements) {
            JsonNode node = mapper.valueToTree(statement);
            assertTrue(schema.validate(node).isEmpty(), statement.accountId());
        }
    }

    private static String accountLine(String id) {
        return id + "Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000";
    }

    private static String customerLine(String id) {
        return id + pad("First", 25) + pad("", 25) + pad("Last", 25)
                + pad("First Street", 50) + pad("", 50) + pad("North Enoshaven", 50)
                + pad("NY", 2) + pad("USA", 3) + pad("12345", 10)
                + pad("", 15) + pad("", 15) + pad("000000000", 9)
                + pad("", 20) + pad("1970-01-01", 10) + pad("", 10) + "Y688" + pad("", 168);
    }

    private static String transaction(String card, String id, String amount) {
        return pad(id, 16) + pad("01", 2) + pad("0001", 4) + pad("POS", 10)
                + pad("Description", 100) + amount
                + pad("", 9 + 50 + 50 + 10) + pad(card, 16)
                + pad("", 26 + 26 + 20);
    }

    private static String pad(String value, int length) {
        return String.format("%-" + length + "s", value);
    }
}
