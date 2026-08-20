package com.aws.carddemo.dataload;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class FixedWidthReader {
  private FixedWidthReader() {}

  static List<byte[]> read(Path path, int recordLength) throws IOException {
    List<byte[]> records = new ArrayList<>();
    try (PushbackInputStream input = new PushbackInputStream(Files.newInputStream(path), 1)) {
      while (true) {
        int first = input.read();
        if (first < 0) {
          break;
        }
        byte[] record = new byte[recordLength];
        record[0] = (byte) first;
        int read = 1;
        while (read < recordLength) {
          int count = input.read(record, read, recordLength - read);
          if (count < 0) {
            throw new IOException("Short fixed-width record in " + path);
          }
          read += count;
        }
        records.add(record);
        int separator = input.read();
        if (separator == '\r') {
          int next = input.read();
          if (next != '\n' && next >= 0) {
            input.unread(next);
          }
        } else if (separator != '\n' && separator >= 0) {
          input.unread(separator);
        }
      }
    }
    return records;
  }
}
