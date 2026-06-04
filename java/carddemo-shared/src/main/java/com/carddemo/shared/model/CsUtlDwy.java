package com.carddemo.shared.model;

/**
 * Java equivalent of the date-utility working-storage copybook {@code CSUTLDWY}
 * ({@code app/cpy/CSUTLDWY.cpy}), which backs the reusable date-edit logic in
 * {@code CSUTLDPY} ({@code app/cpy/CSUTLDPY.cpy}).
 *
 * <p>The copybook splits an 8-character {@code CCYYMMDD} date into century,
 * year, month and day components and carries per-component validity flags plus
 * the formatted validation result. This mutable POJO mirrors that working
 * storage so the validation routine can populate it field by field.</p>
 */
public class CsUtlDwy {

    /** Per-component validity flag, mirroring the {@code FLG-*} 88-levels. */
    public enum FieldFlag {
        /** {@code FLG-*-ISVALID} (COBOL {@code LOW-VALUES}). */
        VALID,
        /** {@code FLG-*-NOT-OK} (COBOL {@code '0'}). */
        NOT_OK,
        /** {@code FLG-*-BLANK} (COBOL {@code 'B'}). */
        BLANK
    }

    /** {@code WS-EDIT-DATE-CCYYMMDD} — the raw date being edited. */
    private String editDateCcyymmdd;

    /** {@code WS-EDIT-DATE-CC} — century, valid values 19 and 20. */
    private int century;
    /** {@code WS-EDIT-DATE-YY} — two digit year within the century. */
    private int year;
    /** {@code WS-EDIT-DATE-MM} — month. */
    private int month;
    /** {@code WS-EDIT-DATE-DD} — day of month. */
    private int day;

    /** {@code WS-EDIT-YEAR-FLG}. */
    private FieldFlag yearFlag = FieldFlag.VALID;
    /** {@code WS-EDIT-MONTH} flag. */
    private FieldFlag monthFlag = FieldFlag.VALID;
    /** {@code WS-EDIT-DAY} flag. */
    private FieldFlag dayFlag = FieldFlag.VALID;

    /** {@code WS-DATE-FORMAT} — defaults to {@code YYYYMMDD}. */
    private String dateFormat = "YYYYMMDD";

    /** {@code WS-DATE-VALIDATION-RESULT} — the formatted result message. */
    private String validationResult;

    /** Valid century values per the {@code THIS-CENTURY}/{@code LAST-CENTURY} 88-levels. */
    public static boolean isValidCentury(int century) {
        return century == 20 || century == 19;
    }

    /** {@code WS-VALID-MONTH}: 1 through 12. */
    public static boolean isValidMonth(int month) {
        return month >= 1 && month <= 12;
    }

    /** {@code WS-31-DAY-MONTH}: months with 31 days. */
    public static boolean is31DayMonth(int month) {
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> true;
            default -> false;
        };
    }

    public String getEditDateCcyymmdd() {
        return editDateCcyymmdd;
    }

    public void setEditDateCcyymmdd(String editDateCcyymmdd) {
        this.editDateCcyymmdd = editDateCcyymmdd;
    }

    public int getCentury() {
        return century;
    }

    public void setCentury(int century) {
        this.century = century;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public FieldFlag getYearFlag() {
        return yearFlag;
    }

    public void setYearFlag(FieldFlag yearFlag) {
        this.yearFlag = yearFlag;
    }

    public FieldFlag getMonthFlag() {
        return monthFlag;
    }

    public void setMonthFlag(FieldFlag monthFlag) {
        this.monthFlag = monthFlag;
    }

    public FieldFlag getDayFlag() {
        return dayFlag;
    }

    public void setDayFlag(FieldFlag dayFlag) {
        this.dayFlag = dayFlag;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(String validationResult) {
        this.validationResult = validationResult;
    }
}
