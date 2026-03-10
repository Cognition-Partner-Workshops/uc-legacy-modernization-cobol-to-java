package com.carddemo.common.io;

/**
 * Functional interface for formatting a typed record into a fixed-width line.
 *
 * @param <T> the record type
 */
@FunctionalInterface
public interface RecordFormatter<T> {

    /**
     * Format a record object into a fixed-width text line.
     *
     * @param record the record to format
     * @return the fixed-width text line
     */
    String format(T record);
}
