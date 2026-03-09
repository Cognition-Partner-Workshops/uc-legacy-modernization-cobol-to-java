package com.carddemo.batch.model;

/**
 * Variable-length record type 1 (12 bytes in COBOL).
 *
 * <pre>
 * 05  VB1-ACCT-ID              PIC 9(11)
 * 05  VB1-ACCT-ACTIVE-STATUS   PIC X(01)
 * </pre>
 */
public record VbRecord1(
        long acctId,
        String acctActiveStatus
) {}
