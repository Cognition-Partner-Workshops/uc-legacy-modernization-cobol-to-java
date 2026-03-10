package com.carddemo.common.model;

/**
 * Maps COBOL copybook CVACT03Y - Card cross-reference (RECLN 50).
 *
 * <pre>
 * 05  XREF-CARD-NUM     PIC X(16)
 * 05  XREF-CUST-ID      PIC 9(09)
 * 05  XREF-ACCT-ID      PIC 9(11)
 * 05  FILLER             PIC X(14)
 * </pre>
 */
public record CardXrefRecord(
        String cardNum,   // PIC X(16)
        String custId,    // PIC 9(09)
        String acctId     // PIC 9(11)
) {
    public static final int RECORD_LENGTH = 50;
    public static final int CARD_NUM_LEN = 16;
    public static final int CUST_ID_LEN = 9;
    public static final int ACCT_ID_LEN = 11;
    public static final int FILLER_LEN = 14;
}
