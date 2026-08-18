package com.carddemo.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class ParityResultTest {

    @Test
    void identicalRecordsMatch() {
        ParityResult result = ParityResult.compare(
                List.of("a", "b"), List.of("a", "b"), List.of("x"), List.of("x"));
        assertEquals(ParityResult.MATCH, result.status());
        assertEquals(0, result.diffLineCount());
        assertNull(result.firstDiffLine());
    }

    @Test
    void differingTextRecordsAreReportedWithTheirLineNumber() {
        ParityResult result = ParityResult.compare(
                List.of("a", "b", "c"), List.of("a", "B", "C"), List.of("x"), List.of("x"));
        assertEquals(ParityResult.DIFF, result.status());
        assertEquals(2, result.diffLineCount());
        assertEquals(2, result.firstDiffLine());
    }

    @Test
    void htmlLineNumbersContinueAfterTheTextRecords() {
        ParityResult result = ParityResult.compare(
                List.of("a", "b"), List.of("a", "b"), List.of("x", "y"), List.of("x", "Y"));
        assertEquals(ParityResult.DIFF, result.status());
        assertEquals(1, result.diffLineCount());
        assertEquals(4, result.firstDiffLine());
    }

    @Test
    void extraRecordsOnOneSideCountAsDifferences() {
        ParityResult result = ParityResult.compare(
                List.of("a", "b"), List.of("a"), List.of("x"), List.of("x"));
        assertEquals(ParityResult.DIFF, result.status());
        assertEquals(1, result.diffLineCount());
        assertEquals(2, result.firstDiffLine());
    }

    @Test
    void anAbsentSideIsMissing() {
        ParityResult result = ParityResult.compare(null, List.of("a"), null, List.of("x"));
        assertEquals(ParityResult.MISSING, result.status());
        assertEquals(0, result.diffLineCount());
        assertNull(result.firstDiffLine());
    }
}
