package carddemo.programs;

import carddemo.JobContext;
import carddemo.cobol.IoStatus;
import carddemo.cobol.KeyedFile;
import carddemo.records.CardRecord;
import java.util.Optional;

/**
 * CBACT02C - reads the card master (CARDFILE, VSAM KSDS keyed on CARD-NUM) sequentially and
 * prints every 150-byte CARD-RECORD to SYSOUT. Any non-00 status on OPEN/READ/CLOSE prints the
 * COBOL diagnostics and abends with code 999.
 */
public final class Cbact02c implements BatchProgram {
    static final String START_MESSAGE = "START OF EXECUTION OF PROGRAM CBACT02C";
    static final String END_MESSAGE = "END OF EXECUTION OF PROGRAM CBACT02C";

    private static final int APPL_AOK = 0;
    private static final int APPL_EOF = 16;

    /** Per-run state mirroring the program's WORKING-STORAGE. */
    private static final class State {
        final JobContext job;
        KeyedFile.Cursor cardFile;
        String cardFileStatus = "00";
        int applResult;
        boolean endOfFile;
        final CardRecord cardRecord = new CardRecord();

        State(JobContext job) {
            this.job = job;
        }
    }

    @Override
    public int run(JobContext job) {
        State ws = new State(job);
        job.display(START_MESSAGE);
        cardFileOpen(ws);

        while (!ws.endOfFile) {
            cardFileGetNext(ws);
            if (!ws.endOfFile) {
                job.display(ws.cardRecord.bytes());
            }
        }

        cardFileClose(ws);
        job.display(END_MESSAGE);
        return 0;
    }

    /** 1000-CARDFILE-GET-NEXT: {@code READ CARDFILE-FILE INTO CARD-RECORD}. */
    private static void cardFileGetNext(State ws) {
        Optional<byte[]> record = ws.cardFile.next();
        if (record.isPresent()) {
            ws.cardFileStatus = "00";
            ws.cardRecord.load(record.get());
            ws.applResult = APPL_AOK;
        } else {
            ws.cardFileStatus = "10";
            ws.applResult = APPL_EOF;
        }
        if (ws.applResult == APPL_AOK) {
            return;
        }
        if (ws.applResult == APPL_EOF) {
            ws.endOfFile = true;
        } else {
            ws.job.display("ERROR READING CARDFILE");
            displayIoStatus(ws);
            abendProgram(ws);
        }
    }

    /** 0000-CARDFILE-OPEN: {@code OPEN INPUT CARDFILE-FILE}. */
    private static void cardFileOpen(State ws) {
        ws.applResult = 8;
        KeyedFile.Opened opened = KeyedFile.open(ws.job.dd("CARDFILE"), CardRecord.LENGTH,
                CardRecord.KEY_OFFSET, CardRecord.KEY_LENGTH);
        ws.cardFileStatus = opened.status();
        if (opened.ok()) {
            ws.cardFile = opened.file().cursor();
            ws.applResult = APPL_AOK;
        } else {
            ws.applResult = 12;
        }
        if (ws.applResult != APPL_AOK) {
            ws.job.display("ERROR OPENING CARDFILE");
            displayIoStatus(ws);
            abendProgram(ws);
        }
    }

    /** 9000-CARDFILE-CLOSE: {@code CLOSE CARDFILE-FILE} (an in-memory KSDS always closes with status 00). */
    private static void cardFileClose(State ws) {
        ws.applResult = 8;
        ws.cardFile = null;
        ws.cardFileStatus = "00";
        ws.applResult = APPL_AOK;
    }

    /** 9999-ABEND-PROGRAM: {@code CALL 'CEE3ABD' USING ABCODE, TIMING} with ABCODE 999, TIMING 0. */
    private static void abendProgram(State ws) {
        ws.job.display("ABENDING PROGRAM");
        throw new Abend("CBACT02C abend, CARDFILE status " + ws.cardFileStatus);
    }

    /** 9910-DISPLAY-IO-STATUS. */
    private static void displayIoStatus(State ws) {
        ws.job.display("FILE STATUS IS: NNNN", IoStatus.displayText(ws.cardFileStatus));
    }
}
