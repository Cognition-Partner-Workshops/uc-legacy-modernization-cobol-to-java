package com.carddemo.statement;

import java.util.List;

/** Per-account comparison outcome, matching the frozen parity contract fields. */
public record ParityResult(String status, int diffLineCount, Integer firstDiffLine) {

    public static final String MATCH = "MATCH";
    public static final String DIFF = "DIFF";
    public static final String MISSING = "MISSING";

    /**
     * Compares text records then HTML records. Line numbers are 1-based over the
     * concatenated text-then-HTML record sequence, so an HTML-only difference reports
     * {@code textRecordCount + htmlLineNumber}.
     */
    public static ParityResult compare(List<String> cobolText, List<String> javaText,
                                       List<String> cobolHtml, List<String> javaHtml) {
        if (cobolText == null || javaText == null || cobolHtml == null || javaHtml == null) {
            return new ParityResult(MISSING, 0, null);
        }
        int diffCount = 0;
        Integer firstDiff = null;

        int textLines = Math.max(cobolText.size(), javaText.size());
        for (int i = 0; i < textLines; i++) {
            if (!lineEquals(cobolText, javaText, i)) {
                diffCount++;
                if (firstDiff == null) {
                    firstDiff = i + 1;
                }
            }
        }
        int textOffset = Math.max(cobolText.size(), javaText.size());
        int htmlLines = Math.max(cobolHtml.size(), javaHtml.size());
        for (int i = 0; i < htmlLines; i++) {
            if (!lineEquals(cobolHtml, javaHtml, i)) {
                diffCount++;
                if (firstDiff == null) {
                    firstDiff = textOffset + i + 1;
                }
            }
        }
        return diffCount == 0
                ? new ParityResult(MATCH, 0, null)
                : new ParityResult(DIFF, diffCount, firstDiff);
    }

    private static boolean lineEquals(List<String> left, List<String> right, int index) {
        String a = index < left.size() ? left.get(index) : null;
        String b = index < right.size() ? right.get(index) : null;
        return a != null && a.equals(b);
    }
}
