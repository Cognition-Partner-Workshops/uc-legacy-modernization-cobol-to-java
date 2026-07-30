package com.carddemo.statement.generator;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StatementGeneratorTest {
  @Test
  void joinsAggregatesAndConformsToContract(@TempDir Path temp) throws Exception {
    Path data = Files.createDirectory(temp.resolve("data"));
    Files.writeString(data.resolve("cardxref.txt"), "11112222333344440000000010000000001\n");
    Files.writeString(data.resolve("custdata.txt"), "000000001John                     Q                       Doe                     "
        + "1 Main St                                          " + " ".repeat(50) + "Town".concat(" ".repeat(46))
        + "NYUSA10001     " + " ".repeat(500 - 9 - 25 - 25 - 25 - 50 - 50 - 50 - 2 - 3 - 10));
    Files.writeString(data.resolve("acctdata.txt"), "00000000001" + " ".repeat(289));
    String tx = "TX00000000000001" + " ".repeat(16 + 4 + 10)
        + "First purchase".concat(" ".repeat(100 - 14)) + "0000000100{"
        + " ".repeat(9 + 50 + 50 + 10 + 16 + 26 + 26 + 20);
    Files.writeString(data.resolve("dailytran.txt"), tx + "\n" + tx.replace("TX00000000000001", "TX00000000000002"));
    Path output = temp.resolve("output");
    var report = new StatementGenerator().generate(data, output);
    assertEquals(1, report.statements());
    JsonNode json = new ObjectMapper().readTree(Files.readString(output.resolve("statement-00000000001.json")));
    assertEquals(5, json.size());
    assertEquals("00000000001", json.get("accountId").asText());
    assertEquals("****-****-****-4444", json.get("cardNumber").asText());
    assertEquals(2, json.get("transactions").size());
    assertEquals(20.0, json.get("totalAmount").asDouble(), 0.001);
    assertFalse(json.has("middleName"));
    assertFalse(json.has("currentBalance"));
  }
}
