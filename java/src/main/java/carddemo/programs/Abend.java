package carddemo.programs;

/**
 * Raised where the COBOL programs perform {@code 9999-ABEND-PROGRAM}
 * ({@code CALL 'CEE3ABD' USING ABCODE, TIMING} with ABCODE 999). The process exits with the
 * abend code, which POSIX truncates to {@code 999 mod 256 = 231}.
 */
public final class Abend extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public static final int ABEND_CODE = 999;

    public Abend(String message) {
        super(message);
    }
}
