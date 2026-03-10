package com.carddemo.common.io;

import com.carddemo.common.model.CardXrefRecord;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardXrefRecordIOTest {

    @Test
    void roundTrip_parseAndFormat() {
        CardXrefRecord original = new CardXrefRecord(
                "4111111111111111",   // cardNum
                "000000001",         // custId
                "00000000001"        // acctId
        );

        String formatted = CardXrefRecordIO.FORMATTER.format(original);
        assertThat(formatted.length()).isEqualTo(CardXrefRecord.RECORD_LENGTH);

        CardXrefRecord parsed = CardXrefRecordIO.PARSER.parse(formatted);
        assertThat(parsed.cardNum()).isEqualTo("4111111111111111");
        assertThat(parsed.custId()).isEqualTo("000000001");
        assertThat(parsed.acctId()).isEqualTo("00000000001");
    }
}
