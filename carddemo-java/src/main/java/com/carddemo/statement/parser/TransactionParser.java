package com.carddemo.statement.parser;

import com.carddemo.statement.model.Transaction;

public final class TransactionParser {
  public Transaction parse(String record) {
    return new Transaction(FixedWidth.field(record, 0, 16), FixedWidth.field(record, 32, 100),
        FixedWidth.zonedDecimal(record, 132, 11), FixedWidth.field(record, 262, 16));
  }
}
