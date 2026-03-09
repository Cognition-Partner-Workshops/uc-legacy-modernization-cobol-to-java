package com.carddemo.batch.io;

/**
 * Thrown when a file I/O operation fails during batch processing.
 * Corresponds to the COBOL 9999-ABEND-PROGRAM paragraph.
 */
public class FileProcessingException extends RuntimeException {

    private final String fileStatus;

    public FileProcessingException(String message, String fileStatus) {
        super(message + " — file status: " + fileStatus);
        this.fileStatus = fileStatus;
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.fileStatus = "N/A";
    }

    public String getFileStatus() {
        return fileStatus;
    }
}
