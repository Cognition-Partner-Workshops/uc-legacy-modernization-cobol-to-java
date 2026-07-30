package com.carddemo.statement;

public final class RecordParsers {
    private RecordParsers() {}

    public static Account account(String line) {
        return new Account(FixedWidth.accountId(FixedWidth.field(line, 1, 11)),
                FixedWidth.amount(FixedWidth.field(line, 13, 24)));
    }

    public static Customer customer(String line) {
        return new Customer(
                FixedWidth.trimmed(line, 1, 9),
                FixedWidth.trimmed(line, 10, 34),
                FixedWidth.trimmed(line, 60, 84),
                FixedWidth.trimmed(line, 85, 134),
                FixedWidth.trimmed(line, 135, 184),
                FixedWidth.trimmed(line, 185, 234),
                FixedWidth.trimmed(line, 235, 236),
                FixedWidth.trimmed(line, 237, 239),
                FixedWidth.trimmed(line, 240, 249),
                Integer.parseInt(FixedWidth.trimmed(line, 330, 332)));
    }

    public static Xref xref(String line) {
        return new Xref(FixedWidth.trimmed(line, 1, 16),
                FixedWidth.trimmed(line, 17, 25),
                FixedWidth.accountId(FixedWidth.field(line, 26, 36)));
    }

    public static Transaction transaction(String line) {
        return new Transaction(FixedWidth.trimmed(line, 263, 278),
                FixedWidth.trimmed(line, 1, 16),
                FixedWidth.trimmed(line, 33, 132),
                FixedWidth.amount(FixedWidth.field(line, 133, 143)));
    }
}
