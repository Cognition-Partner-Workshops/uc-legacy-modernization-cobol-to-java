package com.carddemo.filematch.model;

/**
 * Summary of a file-match processing run.
 * Equivalent to the COBOL DISPLAY summary at the end of CBACT01C / CBTRN02C.
 */
public class ProcessingSummary {

    private final String programName;
    private final int recordsRead;
    private final int recordsMatched;
    private final int recordsRejected;
    private final String matchCriteria;
    private final String status;

    public ProcessingSummary(String programName, int recordsRead, int recordsMatched,
                             int recordsRejected, String matchCriteria, String status) {
        this.programName = programName;
        this.recordsRead = recordsRead;
        this.recordsMatched = recordsMatched;
        this.recordsRejected = recordsRejected;
        this.matchCriteria = matchCriteria;
        this.status = status;
    }

    public String getProgramName() {
        return programName;
    }

    public int getRecordsRead() {
        return recordsRead;
    }

    public int getRecordsMatched() {
        return recordsMatched;
    }

    public int getRecordsRejected() {
        return recordsRejected;
    }

    public String getMatchCriteria() {
        return matchCriteria;
    }

    public String getStatus() {
        return status;
    }
}
