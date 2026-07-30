package com.carddemo.statement.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ParserTest {
  @Test
  void parsesCopybookOffsetsAndZonedAmount() {
    String record = "0000000000683580010001POS TERM  "
        + "Purchase at Abshire-Lowe".formatted()
        + " ".repeat(100 - "Purchase at Abshire-Lowe".length())
        + "0000005047G800000000Abshire-Lowe"
        + " ".repeat(50 - "Abshire-Lowe".length())
        + "North Enoshaven".formatted()
        + " ".repeat(50 - "North Enoshaven".length())
        + "72112     48594526128770652022-06-10 19:27:53.000000"
        + " ".repeat(26);
    var tx = new TransactionParser().parse(record);
    assertEquals("0000000000683580", tx.transactionId());
    assertEquals("Purchase at Abshire-Lowe", tx.description());
    assertEquals(new BigDecimal("504.77"), tx.amount());
    assertEquals("4859452612877065", tx.cardNumber());
  }

  @Test
  void parsesCustomerAddressOffsets() {
    String record = "000000001" + "Immanuel".concat(" ".repeat(17))
        + "Madeline".concat(" ".repeat(17)) + "Kessler".concat(" ".repeat(18))
        + "618 Deshaun Route".concat(" ".repeat(33))
        + "Apt. 802".concat(" ".repeat(42))
        + "Altenwerthshire".concat(" ".repeat(35)) + "NCUSA12546     ";
    var customer = new CustomerParser().parse(record);
    assertEquals("Immanuel", customer.firstName());
    assertEquals("Kessler", customer.lastName());
    assertEquals(java.util.List.of("618 Deshaun Route", "Apt. 802", "Altenwerthshire NC USA 12546"),
        customer.addressLines());
  }
}
