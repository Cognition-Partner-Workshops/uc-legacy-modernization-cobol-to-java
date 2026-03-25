package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * Java equivalent of the COBOL FD OUT-ACCT-REC (fixed-format output).
 * <p>
 * Mirrors COBOL fields with one transformation:
 * - ACCT-REISSUE-DATE is reformatted from YYYY-MM-DD to YYYYMMDD via COBDATFT
 * - If ACCT-CURR-CYC-DEBIT is zero, it is replaced with 2525.00
 */
public record OutAccountRecord(
        long acctId,                    // PIC 9(11)
        String activeStatus,            // PIC X(01)
        BigDecimal currBal,             // PIC S9(10)V99
        BigDecimal creditLimit,         // PIC S9(10)V99
        BigDecimal cashCreditLimit,     // PIC S9(10)V99
        String openDate,                // PIC X(10)
        String expirationDate,          // PIC X(10)
        String reissueDate,             // PIC X(10) -- reformatted by COBDATFT
        BigDecimal currCycCredit,       // PIC S9(10)V99
        BigDecimal currCycDebit,        // PIC S9(10)V99 (COMP-3 in COBOL)
        String groupId                  // PIC X(10)
) {}
