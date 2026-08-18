package com.carddemo.statement;

import java.math.BigDecimal;

/** {@code ACCOUNT-RECORD} from {@code app/cpy/CVACT01Y.cpy} (RECLN 300). */
public record AccountRecord(String accountId, String activeStatus, BigDecimal currentBalance) {

    public static final int RECORD_LENGTH = 300;

    public static AccountRecord parse(String record) {
        String rec = CobolText.alphanumeric(record, RECORD_LENGTH);
        return new AccountRecord(
                CobolText.field(rec, 0, 11),
                CobolText.field(rec, 11, 1),
                ZonedDecimal.decode(CobolText.field(rec, 12, 12), 2));
    }
}
