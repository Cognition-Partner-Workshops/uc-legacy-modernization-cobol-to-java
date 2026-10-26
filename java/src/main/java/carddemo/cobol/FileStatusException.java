package carddemo.cobol;

/** An I/O failure carrying the two-character COBOL FILE STATUS the GnuCOBOL runtime reports for it. */
public final class FileStatusException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String status;

    public FileStatusException(String status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public String status() {
        return status;
    }
}
