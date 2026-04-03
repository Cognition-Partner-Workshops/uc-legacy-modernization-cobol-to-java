package com.carddemo.batch.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BatchFileHelperTest {

    @TempDir
    Path tempDir;

    @Test
    void readAllLinesReadsFile() throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "line1\nline2\nline3\n");
        List<String> lines = BatchFileHelper.readAllLines(file);
        assertEquals(3, lines.size());
        assertEquals("line1", lines.get(0));
    }

    @Test
    void readAllLinesThrowsForMissingFile() {
        Path file = tempDir.resolve("missing.txt");
        assertThrows(IOException.class, () -> BatchFileHelper.readAllLines(file));
    }

    @Test
    void writeAllLinesCreatesFileAndDirectories() throws IOException {
        Path file = tempDir.resolve("subdir/output.txt");
        BatchFileHelper.writeAllLines(file, List.of("hello", "world"));
        assertTrue(Files.exists(file));
        List<String> lines = Files.readAllLines(file);
        assertEquals(2, lines.size());
        assertEquals("hello", lines.get(0));
    }

    @Test
    void fixedWidthPadsShortStrings() {
        assertEquals("ABC       ", BatchFileHelper.fixedWidth("ABC", 10));
        assertEquals("ABCDEFGHIJ", BatchFileHelper.fixedWidth("ABCDEFGHIJK", 10));
        assertEquals("          ", BatchFileHelper.fixedWidth(null, 10));
    }

    @Test
    void parseLongHandlesVariousInputs() {
        assertEquals(12345L, BatchFileHelper.parseLong("12345"));
        assertEquals(0L, BatchFileHelper.parseLong(null));
        assertEquals(0L, BatchFileHelper.parseLong(""));
        assertEquals(0L, BatchFileHelper.parseLong("abc"));
        assertEquals(42L, BatchFileHelper.parseLong("  42  "));
    }
}
