package carddemo.programs;

import carddemo.cobol.Bytes;
import carddemo.records.CodatecnRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Java replacement for the {@code COBDATFT} assembler routine (app/asm/COBDATFT.asm), called with
 * a {@link CodatecnRecord}. Converts YYYYMMDD (type 1) to YYYY-MM-DD (out type 1) or
 * YYYY-MM-DD (type 2) to YYYYMMDD (out type 2) with java.time.
 *
 * <p>The assembler never validates the date: it moves bytes by position and only overwrites the
 * first 8 (or 10) bytes of CODATECN-0UT-DATE. When the input is not a real calendar date, the
 * same positional move is applied so callers see exactly what COBDATFT produced. For a valid
 * date both paths give identical bytes.
 */
public final class DateFormatConverter {
    static final String INVALID_INPUT = "INVALID INPUT";

    private static final DateTimeFormatter DASHED =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter COMPACT =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

    private DateFormatConverter() {
    }

    /** {@code CALL 'COBDATFT' USING CODATECN-REC}. */
    public static void convert(CodatecnRecord rec) {
        char type = rec.getText(CodatecnRecord.TYPE).charAt(0);
        char outType = rec.getText(CodatecnRecord.OUTTYPE).charAt(0);
        String input = rec.getText(CodatecnRecord.INP_DATE);
        byte[] out = rec.get(CodatecnRecord.OUT_DATE);
        if (type == CodatecnRecord.YYYYMMDD_IN && input.charAt(4) != '-'
                && outType != CodatecnRecord.YYYYMMDD_OP) {
            put(out, compactToDashed(input.substring(0, 8)));
        } else if (type == CodatecnRecord.YYYY_MM_DD_IN && outType != CodatecnRecord.YYYY_MM_DD_OP) {
            put(out, dashedToCompact(input.substring(0, 10)));
        } else {
            rec.set(CodatecnRecord.ERROR_MSG, INVALID_INPUT);
            return;
        }
        rec.set(CodatecnRecord.OUT_DATE, out);
    }

    /** {@code YYYY-MM-DD} to {@code YYYYMMDD}; positional (bytes 1-4, 6-7, 9-10) if not a valid date. */
    public static String dashedToCompact(String dashed) {
        try {
            return LocalDate.parse(dashed, DASHED).format(COMPACT);
        } catch (DateTimeParseException e) {
            return dashed.substring(0, 4) + dashed.substring(5, 7) + dashed.substring(8, 10);
        }
    }

    /** {@code YYYYMMDD} to {@code YYYY-MM-DD}; positional (bytes 1-4, 5-6, 7-8) if not a valid date. */
    public static String compactToDashed(String compact) {
        try {
            return LocalDate.parse(compact, COMPACT).format(DASHED);
        } catch (DateTimeParseException e) {
            return compact.substring(0, 4) + "-" + compact.substring(4, 6) + "-" + compact.substring(6, 8);
        }
    }

    private static void put(byte[] out, String converted) {
        byte[] bytes = Bytes.ascii(converted);
        System.arraycopy(bytes, 0, out, 0, bytes.length);
    }
}
