package com.carddemo.statement;

/** {@code CUSTOMER-RECORD} from {@code app/cpy/CUSTREC.cpy} (RECLN 500). */
public record CustomerRecord(
        String customerId,
        String firstName,
        String middleName,
        String lastName,
        String addressLine1,
        String addressLine2,
        String addressLine3,
        String stateCode,
        String countryCode,
        String zip,
        String ficoCreditScore) {

    public static final int RECORD_LENGTH = 500;

    public static CustomerRecord parse(String record) {
        String rec = CobolText.alphanumeric(record, RECORD_LENGTH);
        return new CustomerRecord(
                CobolText.field(rec, 0, 9),
                CobolText.field(rec, 9, 25),
                CobolText.field(rec, 34, 25),
                CobolText.field(rec, 59, 25),
                CobolText.field(rec, 84, 50),
                CobolText.field(rec, 134, 50),
                CobolText.field(rec, 184, 50),
                CobolText.field(rec, 234, 2),
                CobolText.field(rec, 236, 3),
                CobolText.field(rec, 239, 10),
                CobolText.field(rec, 329, 3));
    }
}
