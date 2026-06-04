package com.carddemo.shared.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Java equivalent of the shared date/time copybook {@code CSDAT01Y}
 * ({@code app/cpy/CSDAT01Y.cpy}), which defines the {@code WS-DATE-TIME}
 * structure used across CardDemo programs to hold the current date and time and
 * its various display renderings.
 *
 * <p>The COBOL group fields map as follows:</p>
 * <ul>
 *   <li>{@code WS-CURDATE-YEAR/MONTH/DAY} &rarr; {@link #year()}, {@link #month()}, {@link #day()}</li>
 *   <li>{@code WS-CURTIME-HOURS/MINUTE/SECOND/MILSEC} &rarr; {@link #hours()}, {@link #minute()}, {@link #second()}, {@link #milsec()}</li>
 *   <li>{@code WS-CURDATE-MM-DD-YY} &rarr; {@link #curdateMmDdYy()}</li>
 *   <li>{@code WS-CURTIME-HH-MM-SS} &rarr; {@link #curtimeHhMmSs()}</li>
 *   <li>{@code WS-TIMESTAMP} &rarr; {@link #timestamp()}</li>
 * </ul>
 *
 * @param year   four digit year ({@code WS-CURDATE-YEAR})
 * @param month  month, 1-12 ({@code WS-CURDATE-MONTH})
 * @param day    day of month, 1-31 ({@code WS-CURDATE-DAY})
 * @param hours  hour of day, 0-23 ({@code WS-CURTIME-HOURS})
 * @param minute minute, 0-59 ({@code WS-CURTIME-MINUTE})
 * @param second second, 0-59 ({@code WS-CURTIME-SECOND})
 * @param milsec hundredths of a second, 0-99 ({@code WS-CURTIME-MILSEC})
 */
public record CsDat01y(int year, int month, int day,
                       int hours, int minute, int second, int milsec) {

    private static final DateTimeFormatter MM_DD_YY =
            DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final DateTimeFormatter HH_MM_SS =
            DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    /**
     * Builds a {@link CsDat01y} from a {@link LocalDateTime}, populating the
     * fields the same way the CardDemo programs populate {@code WS-DATE-TIME}
     * from the system clock.
     *
     * @param dateTime the source date/time
     * @return a populated copybook record
     */
    public static CsDat01y from(LocalDateTime dateTime) {
        return new CsDat01y(
                dateTime.getYear(),
                dateTime.getMonthValue(),
                dateTime.getDayOfMonth(),
                dateTime.getHour(),
                dateTime.getMinute(),
                dateTime.getSecond(),
                dateTime.getNano() / 10_000_000);
    }

    /** @return this date/time as a {@link LocalDateTime}. */
    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.of(year, month, day, hours, minute, second,
                milsec * 10_000_000);
    }

    /** @return the {@code WS-CURDATE-N} rendering: {@code YYYYMMDD}. */
    public String curdateN() {
        return String.format("%04d%02d%02d", year, month, day);
    }

    /** @return the {@code WS-CURDATE-MM-DD-YY} rendering: {@code MM/DD/YY}. */
    public String curdateMmDdYy() {
        return toLocalDateTime().format(MM_DD_YY);
    }

    /** @return the {@code WS-CURTIME-HH-MM-SS} rendering: {@code HH:MM:SS}. */
    public String curtimeHhMmSs() {
        return toLocalDateTime().format(HH_MM_SS);
    }

    /**
     * @return the {@code WS-TIMESTAMP} rendering: the 26-character
     *         {@code YYYY-MM-DD HH:MM:SS.MMMMMM} layout (including the 6-digit
     *         fractional-seconds field {@code WS-TIMESTAMP-TM-MS6}).
     */
    public String timestamp() {
        return toLocalDateTime().format(TIMESTAMP);
    }
}
