package carddemo.records;

import carddemo.cobol.RecordArea;

/**
 * {@code CODATECN} CODATECN-REC, 80 bytes: the parameter block of the COBDATFT date-format
 * routine (assembler DSECT {@code COCDATFT} has the same layout).
 */
public final class CodatecnRecord extends RecordArea {
    public static final int LENGTH = 80;

    /** '1' = input YYYYMMDD, '2' = input YYYY-MM-DD. */
    public static final Alnum TYPE = new Alnum(0, 1);
    public static final Alnum INP_DATE = new Alnum(1, 20);
    /** '1' = output YYYY-MM-DD, '2' = output YYYYMMDD. */
    public static final Alnum OUTTYPE = new Alnum(21, 1);
    public static final Alnum OUT_DATE = new Alnum(22, 20);
    public static final Alnum ERROR_MSG = new Alnum(42, 38);

    public static final char YYYYMMDD_IN = '1';
    public static final char YYYY_MM_DD_IN = '2';
    public static final char YYYY_MM_DD_OP = '1';
    public static final char YYYYMMDD_OP = '2';

    /** WORKING-STORAGE item without VALUE clauses: all PIC X, so initially spaces. */
    public CodatecnRecord() {
        super(LENGTH);
    }
}
