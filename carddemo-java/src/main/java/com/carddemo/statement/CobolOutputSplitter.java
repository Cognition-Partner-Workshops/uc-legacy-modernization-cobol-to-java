package com.carddemo.statement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Splits the concatenated COBOL reference output into per-account record lists:
 * the text output on the {@code START OF STATEMENT} banner (ST-LINE0) keyed off the
 * {@code Account ID         :} line, the HTML output on {@code <!DOCTYPE html>}
 * boundaries keyed off the {@code Statement for Account Number:} heading.
 */
public final class CobolOutputSplitter {

    static final String TEXT_BANNER = "START OF STATEMENT";
    static final String ACCOUNT_ID_LABEL = "Account ID         :";
    static final String HTML_BANNER = "<!DOCTYPE html>";
    static final String HTML_ACCOUNT_HEADING = "<h3>Statement for Account Number: ";

    private CobolOutputSplitter() {
    }

    public static Map<String, List<String>> splitText(List<String> records) {
        return split(records, record -> record.contains(TEXT_BANNER), CobolOutputSplitter::textAccountId);
    }

    public static Map<String, List<String>> splitHtml(List<String> records) {
        return split(records, record -> record.strip().equals(HTML_BANNER), CobolOutputSplitter::htmlAccountId);
    }

    private static Map<String, List<String>> split(List<String> records,
                                                   java.util.function.Predicate<String> isBoundary,
                                                   java.util.function.Function<List<String>, String> accountId) {
        Map<String, List<String>> byAccount = new LinkedHashMap<>();
        List<String> current = null;
        for (String record : records) {
            if (isBoundary.test(record)) {
                store(byAccount, current, accountId);
                current = new ArrayList<>();
            }
            if (current != null) {
                current.add(record);
            }
        }
        store(byAccount, current, accountId);
        return byAccount;
    }

    private static void store(Map<String, List<String>> byAccount,
                             List<String> current,
                             java.util.function.Function<List<String>, String> accountId) {
        if (current == null || current.isEmpty()) {
            return;
        }
        String id = accountId.apply(current);
        if (id != null) {
            byAccount.put(id, List.copyOf(current));
        }
    }

    private static String textAccountId(List<String> statement) {
        for (String record : statement) {
            int index = record.indexOf(ACCOUNT_ID_LABEL);
            if (index >= 0) {
                return valueAfter(record, index + ACCOUNT_ID_LABEL.length());
            }
        }
        return null;
    }

    private static String htmlAccountId(List<String> statement) {
        for (String record : statement) {
            int index = record.indexOf(HTML_ACCOUNT_HEADING);
            if (index >= 0) {
                return valueAfter(record, index + HTML_ACCOUNT_HEADING.length());
            }
        }
        return null;
    }

    private static String valueAfter(String record, int start) {
        String tail = record.substring(start).stripLeading();
        int end = 0;
        while (end < tail.length() && Character.isDigit(tail.charAt(end))) {
            end++;
        }
        return end == 0 ? null : tail.substring(0, end);
    }
}
