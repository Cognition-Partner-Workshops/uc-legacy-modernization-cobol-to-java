package com.carddemo.statement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TransactionGrouper {
    private TransactionGrouper() {}

    public static Map<String, List<Transaction>> group(Iterable<String> lines) {
        Map<String, List<Transaction>> grouped = new LinkedHashMap<>();
        for (String line : lines) {
            Transaction transaction = RecordParsers.transaction(line);
            grouped.computeIfAbsent(transaction.cardNumber(), ignored -> new ArrayList<>()).add(transaction);
        }
        return grouped;
    }
}
