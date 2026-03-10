package com.carddemo.common.io;

import com.carddemo.common.model.CardRecord;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardRecordIOTest {

    @Test
    void roundTrip_parseAndFormat() {
        CardRecord original = new CardRecord(
                "4111111111111111",   // cardNum
                "00000000001",       // cardAcctId
                "123",               // cvv
                "JOHN DOE",          // embossedName
                "2025-12-31",        // expirationDate
                "Y"                  // activeStatus
        );

        String formatted = CardRecordIO.FORMATTER.format(original);
        assertThat(formatted.length()).isEqualTo(CardRecord.RECORD_LENGTH);

        CardRecord parsed = CardRecordIO.PARSER.parse(formatted);
        assertThat(parsed.cardNum()).isEqualTo("4111111111111111");
        assertThat(parsed.cardAcctId()).isEqualTo("00000000001");
        assertThat(parsed.cardCvvCd()).isEqualTo("123");
        assertThat(parsed.embossedName()).isEqualTo("JOHN DOE");
        assertThat(parsed.expirationDate()).isEqualTo("2025-12-31");
        assertThat(parsed.activeStatus()).isEqualTo("Y");
    }
}
