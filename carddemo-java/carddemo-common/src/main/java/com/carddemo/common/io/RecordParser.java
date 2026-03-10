package com.carddemo.common.io;

/**
 * Functional interface for parsing a fixed-width line into a typed record.
 *
 * @param <T> the record type
 */
@FunctionalInterface
public interface RecordParser<T> {

    /**
     * Parse a single fixed-width line into a record object.
     *
     * @param line the raw fixed-width text line
     * @return the parsed record
     * @throws IllegalArgumentException if the line cannot be parsed
     */
    T parse(String line);
}
