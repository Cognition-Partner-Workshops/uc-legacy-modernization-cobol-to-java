package com.carddemo.batch.io;

import com.carddemo.batch.model.VbrRecord1;
import com.carddemo.batch.model.VbrRecord2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes variable-length records (VB1 and VB2) to the VBRCFILE.
 * In the COBOL program this file uses RECORDING MODE V with records
 * varying from 10 to 80 bytes. In the Java version each record is
 * written as a pipe-delimited text line prefixed with its type tag.
 */
public final class VbrFileWriter implements AutoCloseable {

    private final BufferedWriter writer;

    public VbrFileWriter(Path path) throws IOException {
        this.writer = Files.newBufferedWriter(path);
    }

    public void writeVb1(VbrRecord1 record) throws IOException {
        writer.write("VB1|" + record.toDelimitedLine());
        writer.newLine();
    }

    public void writeVb2(VbrRecord2 record) throws IOException {
        writer.write("VB2|" + record.toDelimitedLine());
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
