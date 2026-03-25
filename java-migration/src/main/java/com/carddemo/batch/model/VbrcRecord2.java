package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Java equivalent of VBRC-REC2 in CBACT01C (long variable-length record).
 *
 * COBOL layout (39 bytes):
 *   05  VB2-ACCT-ID              PIC 9(11)
 *   05  VB2-ACCT-CURR-BAL        PIC S9(10)V99
 *   05  VB2-ACCT-CREDIT-LIMIT    PIC S9(10)V99
 *   05  VB2-ACCT-REISSUE-YYYY    PIC X(04)
 */
public record VbrcRecord2(
        long acctId,
        BigDecimal currBal,
        BigDecimal creditLimit,
        String reissueYear
) {

    public String toDelimitedLine() {
        return String.join("|",
                String.format("%011d", acctId),
                currBal.toPlainString(),
                creditLimit.toPlainString(),
                reissueYear
        );
    }

    public static VbrcRecord2 fromDelimitedLine(String line) {
        String[] parts = line.split("\\|", -1);
        return new VbrcRecord2(
                Long.parseLong(parts[0]),
                new BigDecimal(parts[1]),
                new BigDecimal(parts[2]),
                parts[3]
        );
    }
}
