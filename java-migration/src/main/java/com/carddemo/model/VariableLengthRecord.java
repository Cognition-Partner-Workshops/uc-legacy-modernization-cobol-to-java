package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Variable-length records written to VBRC-FILE by CBACT01C.
 *
 * The COBOL program writes two variable-length records per account:
 *
 * VB1 (12 bytes):
 *   VB1-ACCT-ID              PIC 9(11)
 *   VB1-ACCT-ACTIVE-STATUS   PIC X(01)
 *
 * VB2 (39 bytes):
 *   VB2-ACCT-ID              PIC 9(11)
 *   VB2-ACCT-CURR-BAL        PIC S9(10)V99
 *   VB2-ACCT-CREDIT-LIMIT    PIC S9(10)V99
 *   VB2-ACCT-REISSUE-YYYY    PIC X(04)
 */
public sealed interface VariableLengthRecord {

    record Type1(long acctId, String activeStatus) implements VariableLengthRecord {
        public static final int LENGTH = 12;
    }

    record Type2(
            long acctId,
            BigDecimal currentBalance,
            BigDecimal creditLimit,
            String reissueYear
    ) implements VariableLengthRecord {
        public static final int LENGTH = 39;
    }
}
