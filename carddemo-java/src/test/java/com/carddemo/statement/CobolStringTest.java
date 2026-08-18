package com.carddemo.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CobolStringTest {

    @Test
    void alphanumericMovePadsAndTruncates() {
        assertEquals("ab   ", CobolText.alphanumeric("ab", 5));
        assertEquals("abcde", CobolText.alphanumeric("abcdefgh", 5));
    }

    @Test
    void fieldExtractionPadsShortRecords() {
        assertEquals("cd  ", CobolText.field("abcd", 2, 4));
        assertEquals("bc", CobolText.field("abcdef", 1, 2));
    }

    @Test
    void delimitedByTruncatesAtFirstOccurrence() {
        assertEquals("Aniya", CobolText.delimitedBy("Aniya                    ", " "));
        assertEquals("Aniya Alba Von", CobolText.delimitedBy("Aniya Alba Von  ", "  "));
        assertEquals("no-delimiter", CobolText.delimitedBy("no-delimiter", "  "));
    }

    @Test
    void nameConcatenationJoinsTokensWithSingleSpacesAndTrailingSpace() {
        String name = StatementGenerator.concatDelimitedBySpace(
                CobolText.alphanumeric("Aniya", 25),
                CobolText.alphanumeric("Alba", 25),
                CobolText.alphanumeric("Von", 25));
        assertEquals("Aniya Alba Von ", name);
    }

    @Test
    void nameConcatenationTruncatesEachTokenAtItsFirstSpace() {
        String name = StatementGenerator.concatDelimitedBySpace(
                CobolText.alphanumeric("Mary Ann", 25),
                CobolText.alphanumeric("", 25),
                CobolText.alphanumeric("Van Dyke", 25));
        assertEquals("Mary  Van ", name);
    }

    @Test
    void addressConcatenationJoinsLineStateCountryAndZip() {
        String address = StatementGenerator.concatDelimitedBySpace(
                CobolText.alphanumeric("New", 50),
                CobolText.alphanumeric("OR", 2),
                CobolText.alphanumeric("USA", 3),
                CobolText.alphanumeric("04257", 10));
        assertEquals("New OR USA 04257 ", address);
    }
}
