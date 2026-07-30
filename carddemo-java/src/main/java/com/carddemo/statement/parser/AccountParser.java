package com.carddemo.statement.parser;

import com.carddemo.statement.model.Account;

public final class AccountParser {
  public Account parse(String record) {
    return new Account(FixedWidth.field(record, 0, 11));
  }
}
