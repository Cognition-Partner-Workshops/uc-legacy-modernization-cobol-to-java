package carddemo.programs;

import carddemo.JobContext;
import carddemo.cobol.FixedFile;
import carddemo.cobol.KeyedFile;
import carddemo.cobol.RecordArea;
import carddemo.records.AccountRecord;
import java.math.BigDecimal;

/**
 * CBACT01C - reads the account master sequentially, prints every account and writes three
 * derived files: a fixed 107-byte extract (OUTFILE), a 110-byte array record (ARRYFILE) and two
 * variable-length records per account (VBRCFILE).
 */
public final class Cbact01c implements BatchProgram {
    static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");
    static final BigDecimal ARRAY_DEBIT_1 = new BigDecimal("1005.00");
    static final BigDecimal ARRAY_DEBIT_2 = new BigDecimal("1525.00");
    static final BigDecimal ARRAY_BAL_3 = new BigDecimal("-1025.00");
    static final BigDecimal ARRAY_DEBIT_3 = new BigDecimal("-2500.00");

    /** OUT-ACCT-REC (107 bytes). */
    public static final class OutRecord extends RecordArea {
        public static final int LENGTH = 107;
        public static final Num ACCT_ID = new Num(0, 11, 0, false);
        public static final Alnum ACTIVE_STATUS = new Alnum(11, 1);
        public static final Num CURR_BAL = new Num(12, 12, 2, true);
        public static final Num CREDIT_LIMIT = new Num(24, 12, 2, true);
        public static final Num CASH_CREDIT_LIMIT = new Num(36, 12, 2, true);
        public static final Alnum OPEN_DATE = new Alnum(48, 10);
        public static final Alnum EXPIRATION_DATE = new Alnum(58, 10);
        public static final Alnum REISSUE_DATE = new Alnum(68, 10);
        public static final Num CURR_CYC_CREDIT = new Num(78, 12, 2, true);
        public static final Packed CURR_CYC_DEBIT = new Packed(90, 12, 2, true);
        public static final Alnum GROUP_ID = new Alnum(97, 10);

        public OutRecord() {
            super(LENGTH);
        }
    }

    /** ARR-ARRAY-REC (110 bytes): account id + 5 x (zoned balance, packed debit) + 4-byte filler. */
    public static final class ArrayRecord extends RecordArea {
        public static final int LENGTH = 110;
        public static final int OCCURS = 5;
        private static final int ENTRY_OFFSET = 11;
        private static final int ENTRY_LENGTH = 19;
        public static final Num ACCT_ID = new Num(0, 11, 0, false);

        public ArrayRecord() {
            super(LENGTH);
        }

        public static Num currBal(int occurrence) {
            return new Num(ENTRY_OFFSET + (occurrence - 1) * ENTRY_LENGTH, 12, 2, true);
        }

        public static Packed currCycDebit(int occurrence) {
            return new Packed(ENTRY_OFFSET + (occurrence - 1) * ENTRY_LENGTH + 12, 12, 2, true);
        }

        /** COBOL INITIALIZE: every elementary numeric item to zero; FILLER is left alone. */
        public void initialize() {
            initialize(ACCT_ID);
            for (int i = 1; i <= OCCURS; i++) {
                initialize(currBal(i));
                initialize(currCycDebit(i));
            }
        }
    }

    /** VBRC-REC1 (12 bytes). */
    public static final class Vbrc1 extends RecordArea {
        public static final int LENGTH = 12;
        public static final Num ACCT_ID = new Num(0, 11, 0, false);
        public static final Alnum ACTIVE_STATUS = new Alnum(11, 1);

        public Vbrc1() {
            super(LENGTH);
        }

        public void initialize() {
            initialize(ACCT_ID);
            initialize(ACTIVE_STATUS);
        }
    }

    /** VBRC-REC2 (39 bytes). */
    public static final class Vbrc2 extends RecordArea {
        public static final int LENGTH = 39;
        public static final Num ACCT_ID = new Num(0, 11, 0, false);
        public static final Num CURR_BAL = new Num(11, 12, 2, true);
        public static final Num CREDIT_LIMIT = new Num(23, 12, 2, true);
        public static final Alnum REISSUE_YYYY = new Alnum(35, 4);

        public Vbrc2() {
            super(LENGTH);
        }
    }

    @Override
    public int run(JobContext job) {
        job.display("START OF EXECUTION OF PROGRAM CBACT01C");
        KeyedFile acctFile = KeyedFile.load(job.dd("ACCTFILE"), AccountRecord.LENGTH,
                AccountRecord.KEY_OFFSET, AccountRecord.KEY_LENGTH);

        OutRecord out = new OutRecord();
        ArrayRecord array = new ArrayRecord();
        Vbrc1 vbrc1 = new Vbrc1();
        Vbrc2 vbrc2 = new Vbrc2();

        try (FixedFile.Writer outFile = new FixedFile.Writer(job.dd("OUTFILE"));
             FixedFile.Writer arrayFile = new FixedFile.Writer(job.dd("ARRYFILE"));
             FixedFile.VariableWriter vbrcFile = new FixedFile.VariableWriter(job.dd("VBRCFILE"))) {
            for (byte[] image : acctFile.records()) {
                AccountRecord acct = new AccountRecord(image);
                array.initialize();
                displayAccount(job, acct);

                populateOutRecord(acct, out);
                outFile.write(out.bytes());

                populateArrayRecord(acct, array);
                arrayFile.write(array.bytes());

                vbrc1.initialize();
                populateVbrcRecords(acct, vbrc1, vbrc2);
                job.display("VBRC-REC1:", vbrc1.bytes());
                job.display("VBRC-REC2:", vbrc2.bytes());
                vbrcFile.write(vbrc1.bytes());
                vbrcFile.write(vbrc2.bytes());

                job.display(acct.bytes());
            }
        }
        job.display("END OF EXECUTION OF PROGRAM CBACT01C");
        return 0;
    }

    private static void displayAccount(JobContext job, AccountRecord acct) {
        job.display("ACCT-ID                 :", acct.get(AccountRecord.ACCT_ID_TEXT));
        job.display("ACCT-ACTIVE-STATUS      :", acct.get(AccountRecord.ACTIVE_STATUS));
        job.display("ACCT-CURR-BAL           :", acct.display(AccountRecord.CURR_BAL));
        job.display("ACCT-CREDIT-LIMIT       :", acct.display(AccountRecord.CREDIT_LIMIT));
        job.display("ACCT-CASH-CREDIT-LIMIT  :", acct.display(AccountRecord.CASH_CREDIT_LIMIT));
        job.display("ACCT-OPEN-DATE          :", acct.get(AccountRecord.OPEN_DATE));
        job.display("ACCT-EXPIRAION-DATE     :", acct.get(AccountRecord.EXPIRATION_DATE));
        job.display("ACCT-REISSUE-DATE       :", acct.get(AccountRecord.REISSUE_DATE));
        job.display("ACCT-CURR-CYC-CREDIT    :", acct.display(AccountRecord.CURR_CYC_CREDIT));
        job.display("ACCT-CURR-CYC-DEBIT     :", acct.display(AccountRecord.CURR_CYC_DEBIT));
        job.display("ACCT-GROUP-ID           :", acct.get(AccountRecord.GROUP_ID));
        job.display("-------------------------------------------------");
    }

    static void populateOutRecord(AccountRecord acct, OutRecord out) {
        out.set(OutRecord.ACCT_ID, acct.get(AccountRecord.ACCT_ID));
        out.set(OutRecord.ACTIVE_STATUS, acct.get(AccountRecord.ACTIVE_STATUS));
        out.set(OutRecord.CURR_BAL, acct.get(AccountRecord.CURR_BAL));
        out.set(OutRecord.CREDIT_LIMIT, acct.get(AccountRecord.CREDIT_LIMIT));
        out.set(OutRecord.CASH_CREDIT_LIMIT, acct.get(AccountRecord.CASH_CREDIT_LIMIT));
        out.set(OutRecord.OPEN_DATE, acct.get(AccountRecord.OPEN_DATE));
        out.set(OutRecord.EXPIRATION_DATE, acct.get(AccountRecord.EXPIRATION_DATE));
        out.set(OutRecord.REISSUE_DATE,
                DateFormatter.dashedToCompact(acct.getText(AccountRecord.REISSUE_DATE)));
        out.set(OutRecord.CURR_CYC_CREDIT, acct.get(AccountRecord.CURR_CYC_CREDIT));
        if (acct.get(AccountRecord.CURR_CYC_DEBIT).signum() == 0) {
            out.set(OutRecord.CURR_CYC_DEBIT, DEFAULT_CYC_DEBIT);
        }
        out.set(OutRecord.GROUP_ID, acct.get(AccountRecord.GROUP_ID));
    }

    static void populateArrayRecord(AccountRecord acct, ArrayRecord array) {
        BigDecimal currBal = acct.get(AccountRecord.CURR_BAL);
        array.set(ArrayRecord.ACCT_ID, acct.get(AccountRecord.ACCT_ID));
        array.set(ArrayRecord.currBal(1), currBal);
        array.set(ArrayRecord.currCycDebit(1), ARRAY_DEBIT_1);
        array.set(ArrayRecord.currBal(2), currBal);
        array.set(ArrayRecord.currCycDebit(2), ARRAY_DEBIT_2);
        array.set(ArrayRecord.currBal(3), ARRAY_BAL_3);
        array.set(ArrayRecord.currCycDebit(3), ARRAY_DEBIT_3);
    }

    static void populateVbrcRecords(AccountRecord acct, Vbrc1 vbrc1, Vbrc2 vbrc2) {
        BigDecimal acctId = acct.get(AccountRecord.ACCT_ID);
        vbrc1.set(Vbrc1.ACCT_ID, acctId);
        vbrc1.set(Vbrc1.ACTIVE_STATUS, acct.get(AccountRecord.ACTIVE_STATUS));
        vbrc2.set(Vbrc2.ACCT_ID, acctId);
        vbrc2.set(Vbrc2.CURR_BAL, acct.get(AccountRecord.CURR_BAL));
        vbrc2.set(Vbrc2.CREDIT_LIMIT, acct.get(AccountRecord.CREDIT_LIMIT));
        vbrc2.set(Vbrc2.REISSUE_YYYY, acct.getText(AccountRecord.REISSUE_DATE).substring(0, 4));
    }
}
