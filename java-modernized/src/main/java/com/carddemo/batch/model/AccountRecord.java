package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Mirrors CVACT01Y.cpy ACCOUNT-RECORD (300-byte fixed-width layout).
 *
 * Field offsets (0-based):
 *   0-10   ACCT-ID              PIC 9(11)
 *  11      ACCT-ACTIVE-STATUS   PIC X(01)
 *  12-23   ACCT-CURR-BAL        PIC S9(10)V99  (signed zoned decimal, 12 bytes)
 *  24-35   ACCT-CREDIT-LIMIT    PIC S9(10)V99
 *  36-47   ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *  48-57   ACCT-OPEN-DATE       PIC X(10)
 *  58-67   ACCT-EXPIRAION-DATE  PIC X(10)
 *  68-77   ACCT-REISSUE-DATE    PIC X(10)
 *  78-89   ACCT-CURR-CYC-CREDIT PIC S9(10)V99
 *  90-101  ACCT-CURR-CYC-DEBIT  PIC S9(10)V99
 * 102-111  ACCT-ADDR-ZIP        PIC X(10)
 * 112-121  ACCT-GROUP-ID        PIC X(10)
 * 122-299  FILLER               PIC X(178)
 */
public class AccountRecord {

    public static final int RECORD_LENGTH = 300;

    private long acctId;
    private char activeStatus;
    private BigDecimal currentBalance;
    private BigDecimal creditLimit;
    private BigDecimal cashCreditLimit;
    private String openDate;
    private String expirationDate;
    private String reissueDate;
    private BigDecimal currentCycleCredit;
    private BigDecimal currentCycleDebit;
    private String addrZip;
    private String groupId;

    public AccountRecord() {}

    public static AccountRecord parseFixedWidth(String line) {
        if (line.length() < RECORD_LENGTH) {
            line = padRight(line, RECORD_LENGTH);
        }

        AccountRecord rec = new AccountRecord();
        rec.acctId = Long.parseLong(line.substring(0, 11).trim().isEmpty()
                ? "0" : line.substring(0, 11).trim());
        rec.activeStatus = line.charAt(11);
        rec.currentBalance = parseSignedDecimal(line.substring(12, 24));
        rec.creditLimit = parseSignedDecimal(line.substring(24, 36));
        rec.cashCreditLimit = parseSignedDecimal(line.substring(36, 48));
        rec.openDate = line.substring(48, 58);
        rec.expirationDate = line.substring(58, 68);
        rec.reissueDate = line.substring(68, 78);
        rec.currentCycleCredit = parseSignedDecimal(line.substring(78, 90));
        rec.currentCycleDebit = parseSignedDecimal(line.substring(90, 102));
        rec.addrZip = line.substring(102, 112);
        rec.groupId = line.substring(112, 122);
        return rec;
    }

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    /**
     * Parses a COBOL S9(10)V99 zoned-decimal field (12 chars, implied 2 decimal places).
     * Supports COBOL overpunch encoding on the last character where the sign is
     * embedded in the final digit ({=+0, A=+1..I=+9, }=-0, J=-1..R=-9),
     * as well as a leading sign character for plain numeric exports.
     */
    static BigDecimal parseSignedDecimal(String raw) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO.setScale(2);
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return BigDecimal.ZERO.setScale(2);
        }

        boolean negative = false;
        if (trimmed.charAt(0) == '-') {
            negative = true;
            trimmed = trimmed.substring(1);
        } else if (trimmed.charAt(0) == '+') {
            trimmed = trimmed.substring(1);
        }

        // Handle COBOL overpunch on last character
        if (!trimmed.isEmpty()) {
            char last = trimmed.charAt(trimmed.length() - 1);
            int posIdx = POSITIVE_OVERPUNCH.indexOf(last);
            int negIdx = NEGATIVE_OVERPUNCH.indexOf(last);
            if (posIdx >= 0) {
                trimmed = trimmed.substring(0, trimmed.length() - 1) + posIdx;
            } else if (negIdx >= 0) {
                negative = true;
                trimmed = trimmed.substring(0, trimmed.length() - 1) + negIdx;
            }
        }

        if (trimmed.contains(".")) {
            BigDecimal val = new BigDecimal(trimmed).setScale(2);
            return negative ? val.negate() : val;
        }

        while (trimmed.length() < 12) {
            trimmed = "0" + trimmed;
        }

        String intPart = trimmed.substring(0, 10);
        String decPart = trimmed.substring(10, 12);
        BigDecimal val = new BigDecimal(intPart + "." + decPart);
        return negative ? val.negate() : val;
    }

    public String toFixedWidth() {
        StringBuilder sb = new StringBuilder(RECORD_LENGTH);
        sb.append(String.format("%011d", acctId));
        sb.append(activeStatus);
        sb.append(formatSignedDecimal(currentBalance));
        sb.append(formatSignedDecimal(creditLimit));
        sb.append(formatSignedDecimal(cashCreditLimit));
        sb.append(padRight(openDate, 10));
        sb.append(padRight(expirationDate, 10));
        sb.append(padRight(reissueDate, 10));
        sb.append(formatSignedDecimal(currentCycleCredit));
        sb.append(formatSignedDecimal(currentCycleDebit));
        sb.append(padRight(addrZip, 10));
        sb.append(padRight(groupId, 10));
        sb.append(padRight("", 178));
        return sb.toString();
    }

    static String formatSignedDecimal(BigDecimal value) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        BigDecimal scaled = value.setScale(2);
        String unscaled = scaled.unscaledValue().abs().toString();
        while (unscaled.length() < 12) {
            unscaled = "0" + unscaled;
        }
        if (scaled.signum() < 0) {
            return "-" + unscaled.substring(1);
        }
        return unscaled;
    }

    private static String padRight(String s, int len) {
        if (s == null) {
            s = "";
        }
        if (s.length() >= len) {
            return s.substring(0, len);
        }
        return s + " ".repeat(len - s.length());
    }

    // Getters and setters

    public long getAcctId() { return acctId; }
    public void setAcctId(long acctId) { this.acctId = acctId; }

    public char getActiveStatus() { return activeStatus; }
    public void setActiveStatus(char activeStatus) { this.activeStatus = activeStatus; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getCashCreditLimit() { return cashCreditLimit; }
    public void setCashCreditLimit(BigDecimal cashCreditLimit) { this.cashCreditLimit = cashCreditLimit; }

    public String getOpenDate() { return openDate; }
    public void setOpenDate(String openDate) { this.openDate = openDate; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public String getReissueDate() { return reissueDate; }
    public void setReissueDate(String reissueDate) { this.reissueDate = reissueDate; }

    public BigDecimal getCurrentCycleCredit() { return currentCycleCredit; }
    public void setCurrentCycleCredit(BigDecimal currentCycleCredit) { this.currentCycleCredit = currentCycleCredit; }

    public BigDecimal getCurrentCycleDebit() { return currentCycleDebit; }
    public void setCurrentCycleDebit(BigDecimal currentCycleDebit) { this.currentCycleDebit = currentCycleDebit; }

    public String getAddrZip() { return addrZip; }
    public void setAddrZip(String addrZip) { this.addrZip = addrZip; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    @Override
    public String toString() {
        return "AccountRecord{" +
                "acctId=" + acctId +
                ", activeStatus=" + activeStatus +
                ", currentBalance=" + currentBalance +
                ", creditLimit=" + creditLimit +
                ", cashCreditLimit=" + cashCreditLimit +
                ", openDate='" + openDate + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                ", reissueDate='" + reissueDate + '\'' +
                ", currentCycleCredit=" + currentCycleCredit +
                ", currentCycleDebit=" + currentCycleDebit +
                ", addrZip='" + addrZip + '\'' +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}
