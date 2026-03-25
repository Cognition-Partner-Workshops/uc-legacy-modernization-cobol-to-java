package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to the two variable-length COBOL records written to VBRCFILE.
 * <p>
 * Record type 1 (short — 12 bytes in COBOL):
 * <pre>
 *   05 VB1-ACCT-ID              PIC 9(11)
 *   05 VB1-ACCT-ACTIVE-STATUS   PIC X(01)
 * </pre>
 * Record type 2 (long — 39 bytes in COBOL):
 * <pre>
 *   05 VB2-ACCT-ID              PIC 9(11)
 *   05 VB2-ACCT-CURR-BAL        PIC S9(10)V99
 *   05 VB2-ACCT-CREDIT-LIMIT    PIC S9(10)V99
 *   05 VB2-ACCT-REISSUE-YYYY    PIC X(04)
 * </pre>
 */
public sealed interface VariableLengthRecord {

    long acctId();

    String toOutputLine();

    /** Short record: account ID + active status. */
    record Type1(long acctId, String activeStatus) implements VariableLengthRecord {
        @Override
        public String toOutputLine() {
            return "VB1|" + String.format("%011d", acctId) + "|" + activeStatus;
        }
    }

    /** Long record: account ID + balance + credit limit + reissue year. */
    record Type2(long acctId, BigDecimal currBal, BigDecimal creditLimit, String reissueYear)
            implements VariableLengthRecord {
        @Override
        public String toOutputLine() {
            return "VB2|" + String.format("%011d", acctId) + "|"
                    + currBal.toPlainString() + "|"
                    + creditLimit.toPlainString() + "|"
                    + reissueYear;
        }
    }
}
