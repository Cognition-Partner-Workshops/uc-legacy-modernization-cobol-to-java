package com.carddemo.batch.cbact01c.model;

/**
 * Java equivalent of COBOL VBRC-REC1 (variable-length record type 1, 12 bytes).
 * <p>
 * Contains account ID and active status only.
 */
public record VbrRecord1(
        long acctId,            // PIC 9(11)
        String activeStatus     // PIC X(01)
) {
    public static final int LENGTH = 12;
}
