package com.carddemo.common.model;

/**
 * Maps COBOL copybook CVACT02Y - Card entity (RECLN 150).
 *
 * <pre>
 * 05  CARD-NUM                PIC X(16)
 * 05  CARD-ACCT-ID            PIC 9(11)
 * 05  CARD-CVV-CD             PIC 9(03)
 * 05  CARD-EMBOSSED-NAME      PIC X(50)
 * 05  CARD-EXPIRAION-DATE     PIC X(10)
 * 05  CARD-ACTIVE-STATUS      PIC X(01)
 * 05  FILLER                  PIC X(59)
 * </pre>
 */
public record CardRecord(
        String cardNum,          // PIC X(16)
        String cardAcctId,       // PIC 9(11)
        String cardCvvCd,        // PIC 9(03)
        String embossedName,     // PIC X(50)
        String expirationDate,   // PIC X(10)
        String activeStatus      // PIC X(01)
) {
    public static final int RECORD_LENGTH = 150;
    public static final int CARD_NUM_LEN = 16;
    public static final int ACCT_ID_LEN = 11;
    public static final int CVV_LEN = 3;
    public static final int EMBOSSED_NAME_LEN = 50;
    public static final int EXPIRATION_DATE_LEN = 10;
    public static final int ACTIVE_STATUS_LEN = 1;
    public static final int FILLER_LEN = 59;
}
