package com.carddemo.statement.parser;

import com.carddemo.statement.model.Customer;

public final class CustomerParser {
  public Customer parse(String record) {
    return new Customer(
        FixedWidth.field(record, 9, 25),
        FixedWidth.field(record, 59, 25),
        FixedWidth.field(record, 84, 50),
        FixedWidth.field(record, 134, 50),
        FixedWidth.field(record, 184, 50),
        FixedWidth.field(record, 234, 2),
        FixedWidth.field(record, 236, 3),
        FixedWidth.field(record, 239, 10));
  }
}
