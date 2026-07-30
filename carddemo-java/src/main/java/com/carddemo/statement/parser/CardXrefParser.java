package com.carddemo.statement.parser;

import com.carddemo.statement.model.CardXref;

public final class CardXrefParser {
  public CardXref parse(String record) {
    return new CardXref(FixedWidth.field(record, 0, 16), FixedWidth.field(record, 16, 9),
        FixedWidth.field(record, 25, 11));
  }
}
