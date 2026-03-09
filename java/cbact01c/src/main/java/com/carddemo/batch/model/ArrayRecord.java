package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Maps to the COBOL FD ARRY-FILE record layout (LRECL 110).
 *
 * <pre>
 *  05  ARR-ACCT-ID                PIC 9(11)
 *  05  ARR-ACCT-BAL OCCURS 5 TIMES
 *    10  ARR-ACCT-CURR-BAL        PIC S9(10)V99           (12 bytes zoned)
 *    10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 COMP-3   (7 bytes packed)
 *  05  ARR-FILLER                 PIC X(04)
 * </pre>
 *
 * Each of the 5 array slots contains a balance (12 bytes) and a packed-decimal
 * debit (7 bytes) = 19 bytes per slot. 11 + (5 * 19) + 4 = 110 bytes.
 */
public record ArrayRecord(
        long acctId,
        BigDecimal[] balances,
        BigDecimal[] cycDebits
) {
    public static final int SLOT_COUNT = 5;

    /**
     * Build an {@code ArrayRecord} from an {@link AccountRecord},
     * applying the business rules from paragraph 1400-POPUL-ARRAY-RECORD:
     *
     * <ul>
     *   <li>Slot 1: balance = account current balance, debit = 1005.00</li>
     *   <li>Slot 2: balance = account current balance, debit = 1525.00</li>
     *   <li>Slot 3: balance = -1025.00, debit = -2500.00</li>
     *   <li>Slots 4-5: zeroes (INITIALIZE sets them to zero)</li>
     * </ul>
     */
    public static ArrayRecord fromAccountRecord(AccountRecord acct) {
        BigDecimal[] balances = new BigDecimal[SLOT_COUNT];
        BigDecimal[] cycDebits = new BigDecimal[SLOT_COUNT];

        Arrays.fill(balances, BigDecimal.ZERO);
        Arrays.fill(cycDebits, BigDecimal.ZERO);

        // Slot 1
        balances[0] = acct.currBal();
        cycDebits[0] = new BigDecimal("1005.00");

        // Slot 2
        balances[1] = acct.currBal();
        cycDebits[1] = new BigDecimal("1525.00");

        // Slot 3
        balances[2] = new BigDecimal("-1025.00");
        cycDebits[2] = new BigDecimal("-2500.00");

        // Slots 4 and 5 stay zero (from INITIALIZE)

        return new ArrayRecord(acct.acctId(), balances, cycDebits);
    }

    /**
     * Format this record as a fixed-width string for writing.
     */
    public String toFixedWidth() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", acctId));

        for (int i = 0; i < SLOT_COUNT; i++) {
            sb.append(formatSigned(balances[i], 10, 2));
            sb.append(formatSigned(cycDebits[i], 10, 2));
        }

        sb.append("    "); // ARR-FILLER PIC X(04)
        return sb.toString();
    }

    private static String formatSigned(BigDecimal value, int intDigits, int decDigits) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs();
        long unscaled = abs.movePointRight(decDigits).longValue();
        int totalDigits = intDigits + decDigits;
        String digits = String.format("%0" + totalDigits + "d", unscaled);

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
