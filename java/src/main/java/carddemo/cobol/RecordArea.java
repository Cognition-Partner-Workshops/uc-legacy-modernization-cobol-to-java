package carddemo.cobol;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * A fixed-length COBOL record area. Fields are described by offset/length descriptors and
 * accessed with MOVE-like semantics so that the byte image of the record matches what the
 * COBOL program would have produced.
 */
public class RecordArea {
    /** {@code PIC X(length)}. */
    public record Alnum(int offset, int length) {
    }

    /** {@code PIC [S]9(digits-scale)V9(scale)} USAGE DISPLAY. */
    public record Num(int offset, int digits, int scale, boolean signed) {
        public int length() {
            return digits;
        }
    }

    /** {@code PIC [S]9(digits-scale)V9(scale)} USAGE COMP-3. */
    public record Packed(int offset, int digits, int scale, boolean signed) {
        public int length() {
            return PackedDecimal.byteLength(digits);
        }
    }

    /** GnuCOBOL allocates FD record areas zero-filled (binary zeros) until the first READ/MOVE. */
    public static final byte FD_FILL = 0x00;

    protected final byte[] data;

    /** WORKING-STORAGE item without VALUE clauses whose elementary items are all alphanumeric (spaces). */
    public RecordArea(int length) {
        this(length, Bytes.SPACE);
    }

    public RecordArea(int length, byte fill) {
        this.data = Bytes.filled(length, fill);
    }

    public RecordArea(byte[] image) {
        this.data = image.clone();
    }

    public int length() {
        return data.length;
    }

    /** Returns a copy of the current byte image. */
    public byte[] bytes() {
        return data.clone();
    }

    /** Copy of the leading {@code length} bytes, e.g. reference modification {@code REC(1:length)}. */
    public byte[] bytes(int length) {
        return Bytes.slice(data, 0, length);
    }

    /** Group MOVE of a whole record image into this area (source truncated or space-padded). */
    public void load(byte[] image) {
        Bytes.moveAlnum(image, data, 0, data.length);
    }

    public byte[] get(Alnum field) {
        return Bytes.slice(data, field.offset(), field.length());
    }

    public String getText(Alnum field) {
        return Bytes.ascii(data, field.offset(), field.length());
    }

    public void set(Alnum field, byte[] value) {
        Bytes.moveAlnum(value, data, field.offset(), field.length());
    }

    public void set(Alnum field, String value) {
        set(field, Bytes.ascii(value));
    }

    public BigDecimal get(Num field) {
        return ZonedDecimal.decode(data, field.offset(), field.digits(), field.scale(), field.signed());
    }

    public boolean isNegative(Num field) {
        return field.signed() && ZonedDecimal.isNegative(data, field.offset(), field.digits());
    }

    public void set(Num field, BigDecimal value) {
        put(field, ZonedDecimal.encode(value, field.digits(), field.scale(), field.signed()));
    }

    public void set(Num field, long value) {
        set(field, BigDecimal.valueOf(value));
    }

    /** {@code MOVE source TO target} between DISPLAY numerics; the source sign is kept (also for -0). */
    public void move(Num target, RecordArea source, Num sourceField) {
        boolean negative = source.isNegative(sourceField);
        put(target, ZonedDecimal.encode(source.get(sourceField), target.digits(), target.scale(),
                target.signed(), negative));
    }

    /** {@code MOVE source TO target} from a DISPLAY numeric into a COMP-3 item. */
    public void move(Packed target, RecordArea source, Num sourceField) {
        set(target, source.get(sourceField));
    }

    /**
     * {@code INITIALIZE} of a numeric DISPLAY item: GnuCOBOL fills it with ASCII zeros and does
     * not over-punch a sign, unlike {@code MOVE 0} which stores a positive-signed zero.
     */
    public void initialize(Num field) {
        Arrays.fill(data, field.offset(), field.offset() + field.digits(), (byte) '0');
    }

    public void initialize(Alnum field) {
        Arrays.fill(data, field.offset(), field.offset() + field.length(), Bytes.SPACE);
    }

    public void initialize(Packed field) {
        set(field, BigDecimal.ZERO);
    }

    /** Text produced by a COBOL {@code DISPLAY} of the numeric field. */
    public String display(Num field) {
        return ZonedDecimal.displayText(get(field), field.digits(), field.scale(), field.signed(),
                isNegative(field));
    }

    public BigDecimal get(Packed field) {
        return PackedDecimal.decode(data, field.offset(), field.digits(), field.scale());
    }

    public void set(Packed field, BigDecimal value) {
        put(field.offset(), PackedDecimal.encode(value, field.digits(), field.scale(), field.signed()));
    }

    private void put(Num field, byte[] encoded) {
        put(field.offset(), encoded);
    }

    private void put(int offset, byte[] encoded) {
        System.arraycopy(encoded, 0, data, offset, encoded.length);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RecordArea area && Arrays.equals(data, area.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
