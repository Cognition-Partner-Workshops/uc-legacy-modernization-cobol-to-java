package com.carddemo.model;

/**
 * Maps to COBOL VBRC-REC1 (12 bytes): ACCT-ID + ACTIVE-STATUS.
 */
public record VbrRecord1(
        long acctId,
        String activeStatus
) {

    public String toOutputLine() {
        return String.format("%011d", acctId) + activeStatus;
    }
}
