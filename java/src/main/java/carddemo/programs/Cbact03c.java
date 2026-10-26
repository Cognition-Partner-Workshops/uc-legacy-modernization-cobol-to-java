package carddemo.programs;

import carddemo.JobContext;
import carddemo.cobol.IoStatus;
import carddemo.cobol.KeyedFile;
import carddemo.records.CardXrefRecord;
import java.util.Optional;

/**
 * CBACT03C - reads the card cross-reference file (XREFFILE, VSAM KSDS keyed on XREF-CARD-NUM)
 * sequentially and prints every 50-byte CARD-XREF-RECORD to SYSOUT. Any non-00 status on
 * OPEN/READ/CLOSE prints the COBOL diagnostics and abends with code 999.
 *
 * <p>The COBOL program displays each record twice: once inside {@code 1000-XREFFILE-GET-NEXT}
 * when the READ returns status 00, and again in the main loop. The port keeps both so SYSOUT
 * matches byte-for-byte.
 */
public final class Cbact03c implements BatchProgram {
    static final String START_MESSAGE = "START OF EXECUTION OF PROGRAM CBACT03C";
    static final String END_MESSAGE = "END OF EXECUTION OF PROGRAM CBACT03C";

    private static final int APPL_AOK = 0;
    private static final int APPL_EOF = 16;

    /** Per-run state mirroring the program's WORKING-STORAGE. */
    private static final class State {
        final JobContext job;
        KeyedFile.Cursor xrefFile;
        String xrefFileStatus = "00";
        int applResult;
        boolean endOfFile;
        final CardXrefRecord cardXrefRecord = new CardXrefRecord();

        State(JobContext job) {
            this.job = job;
        }
    }

    @Override
    public int run(JobContext job) {
        State ws = new State(job);
        job.display(START_MESSAGE);
        xrefFileOpen(ws);

        while (!ws.endOfFile) {
            xrefFileGetNext(ws);
            if (!ws.endOfFile) {
                job.display(ws.cardXrefRecord.bytes());
            }
        }

        xrefFileClose(ws);
        job.display(END_MESSAGE);
        return 0;
    }

    /** 1000-XREFFILE-GET-NEXT: {@code READ XREFFILE-FILE INTO CARD-XREF-RECORD}. */
    private static void xrefFileGetNext(State ws) {
        Optional<byte[]> record = ws.xrefFile.next();
        if (record.isPresent()) {
            ws.xrefFileStatus = "00";
            ws.cardXrefRecord.load(record.get());
            ws.applResult = APPL_AOK;
            ws.job.display(ws.cardXrefRecord.bytes());
        } else {
            ws.xrefFileStatus = "10";
            ws.applResult = APPL_EOF;
        }
        if (ws.applResult == APPL_AOK) {
            return;
        }
        if (ws.applResult == APPL_EOF) {
            ws.endOfFile = true;
        } else {
            ws.job.display("ERROR READING XREFFILE");
            displayIoStatus(ws);
            abendProgram(ws);
        }
    }

    /** 0000-XREFFILE-OPEN: {@code OPEN INPUT XREFFILE-FILE}. */
    private static void xrefFileOpen(State ws) {
        ws.applResult = 8;
        KeyedFile.Opened opened = KeyedFile.open(ws.job.dd("XREFFILE"), CardXrefRecord.LENGTH,
                CardXrefRecord.KEY_OFFSET, CardXrefRecord.KEY_LENGTH);
        ws.xrefFileStatus = opened.status();
        if (opened.ok()) {
            ws.xrefFile = opened.file().cursor();
            ws.applResult = APPL_AOK;
        } else {
            ws.applResult = 12;
        }
        if (ws.applResult != APPL_AOK) {
            ws.job.display("ERROR OPENING XREFFILE");
            displayIoStatus(ws);
            abendProgram(ws);
        }
    }

    /** 9000-XREFFILE-CLOSE: {@code CLOSE XREFFILE-FILE} (an in-memory KSDS always closes with status 00). */
    private static void xrefFileClose(State ws) {
        ws.applResult = 8;
        ws.xrefFile = null;
        ws.xrefFileStatus = "00";
        ws.applResult = APPL_AOK;
    }

    /** 9999-ABEND-PROGRAM: {@code CALL 'CEE3ABD' USING ABCODE, TIMING} with ABCODE 999, TIMING 0. */
    private static void abendProgram(State ws) {
        ws.job.display("ABENDING PROGRAM");
        throw new Abend("CBACT03C abend, XREFFILE status " + ws.xrefFileStatus);
    }

    /** 9910-DISPLAY-IO-STATUS. */
    private static void displayIoStatus(State ws) {
        ws.job.display("FILE STATUS IS: NNNN", IoStatus.displayText(ws.xrefFileStatus));
    }
}
