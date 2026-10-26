package carddemo.records;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CardRecordTest {

    @Test
    void layoutMatchesCvact02y() {
        assertEquals(150, CardRecord.LENGTH);
        assertEquals(0, CardRecord.CARD_NUM.offset());
        assertEquals(16, CardRecord.CARD_ACCT_ID.offset());
        assertEquals(27, CardRecord.CARD_CVV_CD.offset());
        assertEquals(30, CardRecord.CARD_EMBOSSED_NAME.offset());
        assertEquals(80, CardRecord.CARD_EXPIRAION_DATE.offset());
        assertEquals(90, CardRecord.CARD_ACTIVE_STATUS.offset());
        assertEquals(91, CardRecord.FILLER.offset());
        assertEquals(150, CardRecord.FILLER.offset() + CardRecord.FILLER.length());
    }

    @Test
    void decodesAFixtureRecord() {
        String image = "0500024453765740" + "00000000005" + "074" + pad("Aniya Von", 50) + "2023-03-09" + "Y"
                + " ".repeat(59);
        CardRecord card = new CardRecord(image.getBytes(StandardCharsets.US_ASCII));
        assertEquals("0500024453765740", card.getText(CardRecord.CARD_NUM));
        assertEquals(new BigDecimal("5"), card.get(CardRecord.CARD_ACCT_ID));
        assertEquals(new BigDecimal("74"), card.get(CardRecord.CARD_CVV_CD));
        assertEquals("Aniya Von", card.getText(CardRecord.CARD_EMBOSSED_NAME).stripTrailing());
        assertEquals("2023-03-09", card.getText(CardRecord.CARD_EXPIRAION_DATE));
        assertEquals("Y", card.getText(CardRecord.CARD_ACTIVE_STATUS));
    }

    @Test
    void newRecordIsSpaceFilledAndSetsEncodeInPlace() {
        CardRecord card = new CardRecord();
        assertEquals(" ".repeat(150), new String(card.bytes(), StandardCharsets.US_ASCII));
        card.set(CardRecord.CARD_NUM, "4111");
        card.set(CardRecord.CARD_ACCT_ID, 42);
        card.set(CardRecord.CARD_CVV_CD, 7);
        String text = new String(card.bytes(), StandardCharsets.US_ASCII);
        assertEquals("4111            ", text.substring(0, 16));
        assertEquals("00000000042", text.substring(16, 27));
        assertEquals("007", text.substring(27, 30));
    }

    private static String pad(String s, int n) {
        return s + " ".repeat(n - s.length());
    }
}
