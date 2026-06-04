package com.cardemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to OUT-ACCT-REC in the COBOL FD for OUT-FILE.
 * Contains transformed account data written to the flat output file.
 */
public record OutputAccountRecord(
        long acctId,                    // PIC 9(11)
        String activeStatus,            // PIC X(01)
        BigDecimal currBal,             // PIC S9(10)V99
        BigDecimal creditLimit,         // PIC S9(10)V99
        BigDecimal cashCreditLimit,     // PIC S9(10)V99
        String openDate,                // PIC X(10)
        String expirationDate,          // PIC X(10)
        String reissueDate,             // PIC X(10) - reformatted by COBDATFT
        BigDecimal currCycCredit,       // PIC S9(10)V99
        BigDecimal currCycDebit,        // PIC S9(10)V99 COMP-3
        String groupId                  // PIC X(10)
) {
    public String toDelimitedString() {
        return String.join("|",
                String.valueOf(acctId),
                activeStatus,
                currBal.toPlainString(),
                creditLimit.toPlainString(),
                cashCreditLimit.toPlainString(),
                openDate,
                expirationDate,
                reissueDate,
                currCycCredit.toPlainString(),
                currCycDebit.toPlainString(),
                groupId);
    }
}
