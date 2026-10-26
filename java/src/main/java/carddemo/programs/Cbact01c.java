package carddemo.programs;

import carddemo.JobContext;
import carddemo.cobol.FileStatusException;
import carddemo.cobol.KeyedFile;
import carddemo.cobol.RecordArea;
import carddemo.cobol.SequentialFile;
import carddemo.records.AccountRecord;
import carddemo.records.CodatecnRecord;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * CBACT01C - reads the account master (ACCTFILE, VSAM KSDS) sequentially, DISPLAYs every account
 * and writes three derived sequential files: OUTFILE (FB, LRECL 107), ARRYFILE (FB, LRECL 110) and
 * VBRCFILE (VB, two records of 12 and 39 bytes per account). Methods are named after, and follow
 * the order of, the COBOL paragraphs.
 */
public final class Cbact01c implements BatchProgram {
    static final int ABEND_CODE = 999;
    static final String STATUS_OK = "00";
    static final String STATUS_EOF = "10";
    static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");
    static final BigDecimal ARRAY_DEBIT_1 = new BigDecimal("1005.00");
    static final BigDecimal ARRAY_DEBIT_2 = new BigDecimal("1525.00");
    static final BigDecimal ARRAY_BAL_3 = new BigDecimal("-1025.00");
    static final BigDecimal ARRAY_DEBIT_3 = new BigDecimal("-2500.00");

    /** FD OUT-FILE, OUT-ACCT-REC (107 bytes). */
    public static final class OutAcctRec extends RecordArea {
        public static final int LENGTH = 107;
        public static final Num ACCT_ID = new Num(0, 11, 0, false);
        public static final Alnum ACTIVE_STATUS = new Alnum(11, 1);
        public static final Num CURR_BAL = new Num(12, 12, 2, true);
        public static final Num CREDIT_LIMIT = new Num(24, 12, 2, true);
        public static final Num CASH_CREDIT_LIMIT = new Num(36, 12, 2, true);
        public static final Alnum OPEN_DATE = new Alnum(48, 10);
        public static final Alnum EXPIRAION_DATE = new Alnum(58, 10);
        public static final Alnum REISSUE_DATE = new Alnum(68, 10);
        public static final Num CURR_CYC_CREDIT = new Num(78, 12, 2, true);
        public static final Packed CURR_CYC_DEBIT = new Packed(90, 12, 2, true);
        public static final Alnum GROUP_ID = new Alnum(97, 10);

        public OutAcctRec() {
            super(LENGTH, FD_FILL);
        }
    }

    /** FD ARRY-FILE, ARR-ARRAY-REC (110 bytes): id + 5 x (zoned balance, packed debit) + ARR-FILLER. */
    public static final class ArrArrayRec extends RecordArea {
        public static final int LENGTH = 110;
        public static final int OCCURS = 5;
        private static final int ENTRY_OFFSET = 11;
        private static final int ENTRY_LENGTH = 19;
        public static final Num ACCT_ID = new Num(0, 11, 0, false);
        public static final Alnum FILLER = new Alnum(106, 4);

        public ArrArrayRec() {
            super(LENGTH, FD_FILL);
        }

        public static Num currBal(int occurrence) {
            return new Num(ENTRY_OFFSET + (occurrence - 1) * ENTRY_LENGTH, 12, 2, true);
        }

        public static Packed currCycDebit(int occurrence) {
            return new Packed(ENTRY_OFFSET + (occurrence - 1) * ENTRY_LENGTH + 12, 12, 2, true);
        }

        /** {@code INITIALIZE ARR-ARRAY-REC}: numerics to zero, ARR-FILLER (a named item) to spaces. */
        public void initialize() {
            initialize(ACCT_ID);
            for (int i = 1; i <= OCCURS; i++) {
                initialize(currBal(i));
                initialize(currCycDebit(i));
            }
            initialize(FILLER);
        }
    }

    /** WORKING-STORAGE VBRC-REC1 (12 bytes). */
    public static final class VbrcRec1 extends RecordArea {
        public static final int LENGTH = 12;
        public static final Num ACCT_ID = new Num(0, 11, 0, false);
        public static final Alnum ACTIVE_STATUS = new Alnum(11, 1);

        public VbrcRec1() {
            super(LENGTH);
            initialize();
        }

        public void initialize() {
            initialize(ACCT_ID);
            initialize(ACTIVE_STATUS);
        }
    }

    /** WORKING-STORAGE VBRC-REC2 (39 bytes). */
    public static final class VbrcRec2 extends RecordArea {
        public static final int LENGTH = 39;
        public static final Num ACCT_ID = new Num(0, 11, 0, false);
        public static final Num CURR_BAL = new Num(11, 12, 2, true);
        public static final Num CREDIT_LIMIT = new Num(23, 12, 2, true);
        public static final Alnum REISSUE_YYYY = new Alnum(35, 4);

        public VbrcRec2() {
            super(LENGTH);
            initialize(ACCT_ID);
            initialize(CURR_BAL);
            initialize(CREDIT_LIMIT);
        }
    }

    /** WORKING-STORAGE WS-ACCT-REISSUE-DATE / WS-REISSUE-DATE (10 bytes). */
    static final class WsReissueDate extends RecordArea {
        static final Alnum WHOLE = new Alnum(0, 10);
        static final Alnum YYYY = new Alnum(0, 4);

        WsReissueDate() {
            super(10);
        }
    }

    private final AccountRecord accountRecord = new AccountRecord();
    private final CodatecnRecord codatecnRec = new CodatecnRecord();
    private final OutAcctRec outAcctRec = new OutAcctRec();
    private final ArrArrayRec arrArrayRec = new ArrArrayRec();
    private final VbrcRec1 vbrcRec1 = new VbrcRec1();
    private final VbrcRec2 vbrcRec2 = new VbrcRec2();
    private final WsReissueDate wsReissueDate = new WsReissueDate();

    private JobContext job;
    private KeyedFile acctFile;
    private SequentialFile outFile;
    private SequentialFile arryFile;
    private SequentialFile vbrcFile;
    private boolean endOfFile;

    private boolean started;

    /**
     * One call per instance, like one COBOL run unit: the FD record areas carry state from record
     * to record (see {@link #populAcctRecord()}), so a rerun needs a fresh instance.
     */
    @Override
    public int run(JobContext jobContext) {
        if (started) {
            throw new IllegalStateException("Cbact01c instances are single-use; create a new one per run");
        }
        started = true;
        this.job = jobContext;
        boolean completed = false;
        try {
            job.display("START OF EXECUTION OF PROGRAM CBACT01C");
            acctFileOpen();
            outFileOpen();
            arrFileOpen();
            vbrFileOpen();

            while (!endOfFile) {
                acctFileGetNext();
                if (!endOfFile) {
                    job.display(accountRecord.bytes());
                }
            }

            acctFileClose();
            job.display("END OF EXECUTION OF PROGRAM CBACT01C");
            completed = true;
        } catch (RuntimeException failure) {
            closeOutputs().ifPresent(failure::addSuppressed);
            throw failure;
        } finally {
            if (!completed) {
                closeOutputs();
            }
        }
        Optional<IllegalStateException> closeFailure = closeOutputs();
        if (closeFailure.isPresent()) {
            throw closeFailure.get();
        }
        return 0;
    }

    /** 1000-ACCTFILE-GET-NEXT. */
    void acctFileGetNext() {
        byte[] record = acctFile.readNext();
        if (record == null) {
            endOfFile = true;
            return;
        }
        accountRecord.load(record);
        arrArrayRec.initialize();
        displayAcctRecord();
        populAcctRecord();
        writeAcctRecord();
        populArrayRecord();
        writeArryRecord();
        vbrcRec1.initialize();
        populVbrcRecord();
        writeVb1Record();
        writeVb2Record();
    }

    /** 1100-DISPLAY-ACCT-RECORD. */
    void displayAcctRecord() {
        job.display("ACCT-ID                 :", accountRecord.display(AccountRecord.ACCT_ID));
        job.display("ACCT-ACTIVE-STATUS      :", accountRecord.get(AccountRecord.ACTIVE_STATUS));
        job.display("ACCT-CURR-BAL           :", accountRecord.display(AccountRecord.CURR_BAL));
        job.display("ACCT-CREDIT-LIMIT       :", accountRecord.display(AccountRecord.CREDIT_LIMIT));
        job.display("ACCT-CASH-CREDIT-LIMIT  :", accountRecord.display(AccountRecord.CASH_CREDIT_LIMIT));
        job.display("ACCT-OPEN-DATE          :", accountRecord.get(AccountRecord.OPEN_DATE));
        job.display("ACCT-EXPIRAION-DATE     :", accountRecord.get(AccountRecord.EXPIRAION_DATE));
        job.display("ACCT-REISSUE-DATE       :", accountRecord.get(AccountRecord.REISSUE_DATE));
        job.display("ACCT-CURR-CYC-CREDIT    :", accountRecord.display(AccountRecord.CURR_CYC_CREDIT));
        job.display("ACCT-CURR-CYC-DEBIT     :", accountRecord.display(AccountRecord.CURR_CYC_DEBIT));
        job.display("ACCT-GROUP-ID           :", accountRecord.get(AccountRecord.GROUP_ID));
        job.display("-------------------------------------------------");
    }

    /**
     * 1300-POPUL-ACCT-RECORD. OUT-ACCT-CURR-CYC-DEBIT is only assigned when the account's debit is
     * zero (no ELSE in the COBOL), so otherwise the record area keeps its previous content.
     */
    void populAcctRecord() {
        outAcctRec.move(OutAcctRec.ACCT_ID, accountRecord, AccountRecord.ACCT_ID);
        outAcctRec.set(OutAcctRec.ACTIVE_STATUS, accountRecord.get(AccountRecord.ACTIVE_STATUS));
        outAcctRec.move(OutAcctRec.CURR_BAL, accountRecord, AccountRecord.CURR_BAL);
        outAcctRec.move(OutAcctRec.CREDIT_LIMIT, accountRecord, AccountRecord.CREDIT_LIMIT);
        outAcctRec.move(OutAcctRec.CASH_CREDIT_LIMIT, accountRecord, AccountRecord.CASH_CREDIT_LIMIT);
        outAcctRec.set(OutAcctRec.OPEN_DATE, accountRecord.get(AccountRecord.OPEN_DATE));
        outAcctRec.set(OutAcctRec.EXPIRAION_DATE, accountRecord.get(AccountRecord.EXPIRAION_DATE));
        byte[] reissueDate = accountRecord.get(AccountRecord.REISSUE_DATE);
        codatecnRec.set(CodatecnRecord.INP_DATE, reissueDate);
        wsReissueDate.set(WsReissueDate.WHOLE, reissueDate);
        codatecnRec.set(CodatecnRecord.TYPE, String.valueOf(CodatecnRecord.YYYY_MM_DD_IN));
        codatecnRec.set(CodatecnRecord.OUTTYPE, String.valueOf(CodatecnRecord.YYYYMMDD_OP));

        DateFormatConverter.convert(codatecnRec);

        outAcctRec.set(OutAcctRec.REISSUE_DATE, codatecnRec.get(CodatecnRecord.OUT_DATE));
        outAcctRec.move(OutAcctRec.CURR_CYC_CREDIT, accountRecord, AccountRecord.CURR_CYC_CREDIT);
        if (accountRecord.get(AccountRecord.CURR_CYC_DEBIT).signum() == 0) {
            outAcctRec.set(OutAcctRec.CURR_CYC_DEBIT, DEFAULT_CYC_DEBIT);
        }
        outAcctRec.set(OutAcctRec.GROUP_ID, accountRecord.get(AccountRecord.GROUP_ID));
    }

    /** 1350-WRITE-ACCT-RECORD. */
    void writeAcctRecord() {
        write(outFile, outAcctRec.bytes());
    }

    /** 1400-POPUL-ARRAY-RECORD. Occurrences 4 and 5 keep their INITIALIZE values. */
    void populArrayRecord() {
        arrArrayRec.move(ArrArrayRec.ACCT_ID, accountRecord, AccountRecord.ACCT_ID);
        arrArrayRec.move(ArrArrayRec.currBal(1), accountRecord, AccountRecord.CURR_BAL);
        arrArrayRec.set(ArrArrayRec.currCycDebit(1), ARRAY_DEBIT_1);
        arrArrayRec.move(ArrArrayRec.currBal(2), accountRecord, AccountRecord.CURR_BAL);
        arrArrayRec.set(ArrArrayRec.currCycDebit(2), ARRAY_DEBIT_2);
        arrArrayRec.set(ArrArrayRec.currBal(3), ARRAY_BAL_3);
        arrArrayRec.set(ArrArrayRec.currCycDebit(3), ARRAY_DEBIT_3);
    }

    /** 1450-WRITE-ARRY-RECORD. */
    void writeArryRecord() {
        write(arryFile, arrArrayRec.bytes());
    }

    /** 1500-POPUL-VBRC-RECORD. */
    void populVbrcRecord() {
        vbrcRec1.move(VbrcRec1.ACCT_ID, accountRecord, AccountRecord.ACCT_ID);
        vbrcRec2.move(VbrcRec2.ACCT_ID, accountRecord, AccountRecord.ACCT_ID);
        vbrcRec1.set(VbrcRec1.ACTIVE_STATUS, accountRecord.get(AccountRecord.ACTIVE_STATUS));
        vbrcRec2.move(VbrcRec2.CURR_BAL, accountRecord, AccountRecord.CURR_BAL);
        vbrcRec2.move(VbrcRec2.CREDIT_LIMIT, accountRecord, AccountRecord.CREDIT_LIMIT);
        vbrcRec2.set(VbrcRec2.REISSUE_YYYY, wsReissueDate.get(WsReissueDate.YYYY));
        job.display("VBRC-REC1:", vbrcRec1.bytes());
        job.display("VBRC-REC2:", vbrcRec2.bytes());
    }

    /** 1550-WRITE-VB1-RECORD (WS-RECD-LEN = 12). */
    void writeVb1Record() {
        write(vbrcFile, vbrcRec1.bytes(VbrcRec1.LENGTH));
    }

    /** 1575-WRITE-VB2-RECORD (WS-RECD-LEN = 39). */
    void writeVb2Record() {
        write(vbrcFile, vbrcRec2.bytes(VbrcRec2.LENGTH));
    }

    /** 0000-ACCTFILE-OPEN. */
    void acctFileOpen() {
        try {
            acctFile = KeyedFile.openInput(job.dd("ACCTFILE"), AccountRecord.LENGTH,
                    AccountRecord.KEY_OFFSET, AccountRecord.KEY_LENGTH);
        } catch (FileStatusException e) {
            job.display("ERROR OPENING ACCTFILE");
            ioError(e.status());
        }
    }

    /** 2000-OUTFILE-OPEN. */
    void outFileOpen() {
        try {
            outFile = SequentialFile.openFixed(job.dd("OUTFILE"));
        } catch (FileStatusException e) {
            job.display("ERROR OPENING OUTFILE", e.status());
            ioError(e.status());
        }
    }

    /** 3000-ARRFILE-OPEN. */
    void arrFileOpen() {
        try {
            arryFile = SequentialFile.openFixed(job.dd("ARRYFILE"));
        } catch (FileStatusException e) {
            job.display("ERROR OPENING ARRAYFILE", e.status());
            ioError(e.status());
        }
    }

    /** 4000-VBRFILE-OPEN. */
    void vbrFileOpen() {
        try {
            vbrcFile = SequentialFile.openVariable(job.dd("VBRCFILE"));
        } catch (FileStatusException e) {
            job.display("ERROR OPENING VBRC FILE", e.status());
            ioError(e.status());
        }
    }

    /** 9000-ACCTFILE-CLOSE: the unloaded KSDS image needs no close; status is always 00. */
    void acctFileClose() {
        acctFile = null;
    }

    /** Shared tail of 1350/1450/1550/1575: status other than 00/10 abends. */
    private void write(SequentialFile file, byte[] record) {
        try {
            file.write(record);
        } catch (FileStatusException e) {
            job.display("ACCOUNT FILE WRITE STATUS IS:", e.status());
            ioError(e.status());
        }
    }

    private void ioError(String status) {
        displayIoStatus(status);
        abendProgram();
    }

    /** 9999-ABEND-PROGRAM: CALL 'CEE3ABD' USING ABCODE (999), TIMING (0). */
    void abendProgram() {
        job.display("ABENDING PROGRAM");
        throw new Abend(ABEND_CODE);
    }

    /** 9910-DISPLAY-IO-STATUS. */
    void displayIoStatus(String ioStatus) {
        job.display("FILE STATUS IS: NNNN" + ioStatus04(ioStatus));
    }

    /**
     * IO-STATUS-04 as built by 9910-DISPLAY-IO-STATUS: {@code 00nn} for a numeric status, else
     * status byte 1 followed by the binary value of status byte 2 as three digits.
     */
    static String ioStatus04(String ioStatus) {
        char stat1 = ioStatus.charAt(0);
        char stat2 = ioStatus.charAt(1);
        boolean numeric = Character.isDigit(stat1) && Character.isDigit(stat2);
        if (!numeric || stat1 == '9') {
            return stat1 + String.format("%03d", (stat2 & 0xFF) % 1000);
        }
        return "00" + ioStatus;
    }

    /**
     * Implicit CLOSE of the output files at GOBACK / STOP RUN. Every opened file is closed even if
     * an earlier close fails; the failures are returned rather than thrown so an abend in flight
     * keeps its code.
     */
    private Optional<IllegalStateException> closeOutputs() {
        IllegalStateException failure = null;
        for (SequentialFile file : new SequentialFile[] {outFile, arryFile, vbrcFile}) {
            if (file == null) {
                continue;
            }
            try {
                file.close();
            } catch (FileStatusException e) {
                if (failure == null) {
                    failure = new IllegalStateException("implicit CLOSE failed", e);
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        outFile = null;
        arryFile = null;
        vbrcFile = null;
        return Optional.ofNullable(failure);
    }
}
