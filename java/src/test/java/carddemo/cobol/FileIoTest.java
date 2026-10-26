package carddemo.cobol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileIoTest {
    @TempDir
    Path dir;

    @Test
    void fixedRecordsAreWrittenBackToBack() throws Exception {
        Path path = dir.resolve("fb");
        try (SequentialFile file = SequentialFile.openFixed(path)) {
            file.write(Bytes.ascii("ABC"));
            file.write(Bytes.ascii("DEF"));
        }
        assertEquals("ABCDEF", Files.readString(path));
    }

    @Test
    void variableRecordsCarryGnuCobolHeader() throws Exception {
        Path path = dir.resolve("vb");
        try (SequentialFile file = SequentialFile.openVariable(path)) {
            file.write(Bytes.ascii("HELLO"));
            file.write(new byte[300]);
        }
        byte[] image = Files.readAllBytes(path);
        assertEquals(4 + 5 + 4 + 300, image.length);
        assertArrayEquals(new byte[] {0, 5, 0, 0}, Bytes.slice(image, 0, 4));
        assertArrayEquals(new byte[] {1, 44, 0, 0}, Bytes.slice(image, 9, 4));
    }

    @Test
    void openOutputFailureStatus() {
        FileStatusException e = assertThrows(FileStatusException.class,
                () -> SequentialFile.openFixed(dir.resolve("missing/dir/out")));
        assertEquals("30", e.status());
        assertEquals("37", assertThrows(FileStatusException.class, () -> SequentialFile.openFixed(dir)).status());
    }

    @Test
    void keyedFileReadsInUnsignedKeyOrder() throws Exception {
        Path path = dir.resolve("ksds");
        Files.write(path, Bytes.ascii("30b20a10c"));
        KeyedFile file = KeyedFile.openInput(path, 3, 0, 2);
        assertEquals("10c", new String(file.readNext()));
        assertEquals("20a", new String(file.readNext()));
        assertEquals("30b", new String(file.readNext()));
        assertNull(file.readNext());
    }

    @Test
    void keyedFileStatuses() throws IOException {
        assertEquals("35", assertThrows(FileStatusException.class,
                () -> KeyedFile.openInput(dir.resolve("none"), 3, 0, 2)).status());
        Path ragged = Files.write(dir.resolve("ragged"), Bytes.ascii("10a2"));
        assertEquals("30", assertThrows(FileStatusException.class,
                () -> KeyedFile.openInput(ragged, 3, 0, 2)).status());
        Path dup = Files.write(dir.resolve("dup"), Bytes.ascii("10a10b"));
        assertEquals("30", assertThrows(FileStatusException.class,
                () -> KeyedFile.openInput(dup, 3, 0, 2)).status());
    }
}
