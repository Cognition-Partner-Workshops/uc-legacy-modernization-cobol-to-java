package carddemo.programs;

import carddemo.JobContext;

/** Raised where the COBOL programs perform {@code 9999-ABEND-PROGRAM} (CEE3ABD with code 999). */
public final class Abend extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public static final int ABEND_CODE = 999;

    public Abend(String message) {
        super(message);
    }

    /** Mirrors the COBOL error path: message, {@code 9910-DISPLAY-IO-STATUS}, then the abend. */
    public static Abend ioError(JobContext job, String message, String fileStatus) {
        job.display(message);
        job.display("FILE STATUS IS: NNNN" + "00" + fileStatus);
        job.display("ABENDING PROGRAM");
        return new Abend(message + " (status " + fileStatus + ")");
    }
}
