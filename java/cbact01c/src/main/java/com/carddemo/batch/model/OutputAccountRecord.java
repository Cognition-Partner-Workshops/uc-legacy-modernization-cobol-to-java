package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL FD OUT-FILE record layout (LRECL 107).
 *
 * <pre>
 *  05  OUT-ACCT-ID                PIC 9(11)
 *  05  OUT-ACCT-ACTIVE-STATUS     PIC X(01)
 *  05  OUT-ACCT-CURR-BAL          PIC S9(10)V99
 *  05  OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99
 *  05  OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *  05  OUT-ACCT-OPEN-DATE         PIC X(10)
 *  05  OUT-ACCT-EXPIRAION-DATE    PIC X(10)
 *  05  OUT-ACCT-REISSUE-DATE      PIC X(10)
 *  05  OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99
 *  05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99  USAGE IS COMP-3
 *  05  OUT-ACCT-GROUP-ID          PIC X(10)
 * </pre>
 *
 * Note: The COBOL COMP-3 field (packed decimal) for cycle debit uses
 * 7 bytes on the mainframe. The JCL DCB shows LRECL=107.
 * In our Java version we model the logical values and format them
 * in the writer.
 */
public record OutputAccountRecord(
        long acctId,
        String activeStatus,
        BigDecimal currBal,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currCycCredit,
        BigDecimal currCycDebit,
        String groupId
) {

    /**
     * Build an {@code OutputAccountRecord} from an {@link AccountRecord},
     * applying the business rules from paragraph 1300-POPUL-ACCT-RECORD:
     *
     * <ul>
     *   <li>Reissue date is reformatted from YYYY-MM-DD to YYYYMMDD
     *       (simulating the COBDATFT call with type=2, outtype=2).</li>
     *   <li>If the source cycle debit is zero the output defaults to 2525.00.</li>
     * </ul>
     */
    public static OutputAccountRecord fromAccountRecord(AccountRecord acct) {
        String reformattedReissueDate = DateConverter.convertDate(
                acct.reissueDate(), "2", "2");

        BigDecimal cycDebit = acct.currCycDebit().signum() == 0
                ? new BigDecimal("2525.00")
                : acct.currCycDebit();

        return new OutputAccountRecord(
                acct.acctId(),
                acct.activeStatus(),
                acct.currBal(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                reformattedReissueDate,
                acct.currCycCredit(),
                cycDebit,
                acct.groupId()
        );
    }

    /**
     * Format this record as a fixed-width string matching the COBOL output layout.
     * <p>
     * All signed numeric fields are formatted as sign + digits (e.g. "+0000001940.00")
     * in a human-readable representation. The COMP-3 cycle debit is rendered the same way
     * (the binary packing is a mainframe storage concern, not a logical-value concern).
     */
    public String toFixedWidth() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", acctId));
        sb.append(activeStatus);
        sb.append(formatSigned(currBal, 10, 2));
        sb.append(formatSigned(creditLimit, 10, 2));
        sb.append(formatSigned(cashCreditLimit, 10, 2));
        sb.append(String.format("%-10s", openDate));
        sb.append(String.format("%-10s", expirationDate));
        sb.append(String.format("%-10s", reissueDate));
        sb.append(formatSigned(currCycCredit, 10, 2));
        sb.append(formatSigned(currCycDebit, 10, 2));
        sb.append(String.format("%-10s", groupId));
        return sb.toString();
    }

    /**
     * Format a signed decimal to COBOL-style zoned-decimal with trailing overpunch.
     */
    private static String formatSigned(BigDecimal value, int intDigits, int decDigits) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs();
        // Scale to remove decimal point: e.g. 1940.00 -> 194000
        long unscaled = abs.movePointRight(decDigits).longValue();
        int totalDigits = intDigits + decDigits;
        String digits = String.format("%0" + totalDigits + "d", unscaled);

        // Apply trailing overpunch sign
        char lastChar = digits.charAt(digits.length() - 1);
        int lastDigit = lastChar - '0';
        char overpunch;
        if (negative) {
            overpunch = lastDigit == 0 ? '}' : (char) ('J' + lastDigit - 1);
        } else {
            overpunch = lastDigit == 0 ? '{' : (char) ('A' + lastDigit - 1);
        }
        return digits.substring(0, digits.length() - 1) + overpunch;
    }
}
