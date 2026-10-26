package carddemo.records;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CardXrefRecordTest {

    @Test
    void layoutMatchesCvact03y() {
        assertEquals(50, CardXrefRecord.LENGTH);
        assertEquals(0, CardXrefRecord.XREF_CARD_NUM.offset());
        assertEquals(16, CardXrefRecord.XREF_CARD_NUM.length());
        assertEquals(16, CardXrefRecord.XREF_CUST_ID.offset());
        assertEquals(9, CardXrefRecord.XREF_CUST_ID.length());
        assertEquals(25, CardXrefRecord.XREF_ACCT_ID.offset());
        assertEquals(11, CardXrefRecord.XREF_ACCT_ID.length());
        assertEquals(36, CardXrefRecord.FILLER.offset());
        assertEquals(50, CardXrefRecord.FILLER.offset() + CardXrefRecord.FILLER.length());
        assertEquals(CardXrefRecord.XREF_CARD_NUM.offset(), CardXrefRecord.KEY_OFFSET);
        assertEquals(CardXrefRecord.XREF_CARD_NUM.length(), CardXrefRecord.KEY_LENGTH);
    }

    @Test
    void decodesAFixtureRecord() {
        String image = "0500024453765740" + "000000050" + "00000000050" + " ".repeat(14);
        CardXrefRecord xref = new CardXrefRecord(image.getBytes(StandardCharsets.US_ASCII));
        assertEquals("0500024453765740", xref.getText(CardXrefRecord.XREF_CARD_NUM));
        assertEquals(new BigDecimal("50"), xref.get(CardXrefRecord.XREF_CUST_ID));
        assertEquals(new BigDecimal("50"), xref.get(CardXrefRecord.XREF_ACCT_ID));
        assertEquals(" ".repeat(14), xref.getText(CardXrefRecord.FILLER));
    }

    @Test
    void newRecordIsSpaceFilledAndSetsEncodeInPlace() {
        CardXrefRecord xref = new CardXrefRecord();
        assertEquals(" ".repeat(50), new String(xref.bytes(), StandardCharsets.US_ASCII));
        xref.set(CardXrefRecord.XREF_CARD_NUM, "4111");
        xref.set(CardXrefRecord.XREF_CUST_ID, 42);
        xref.set(CardXrefRecord.XREF_ACCT_ID, 7);
        String text = new String(xref.bytes(), StandardCharsets.US_ASCII);
        assertEquals("4111            ", text.substring(0, 16));
        assertEquals("000000042", text.substring(16, 25));
        assertEquals("00000000007", text.substring(25, 36));
        assertEquals(" ".repeat(14), text.substring(36));
    }
}
