package carddemo.programs;

/** Raised where a COBOL program calls the CEE3ABD abend service; ends the step with the abend code. */
public final class Abend extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int abendCode;

    public Abend(int abendCode) {
        super("abend U" + String.format("%04d", abendCode));
        this.abendCode = abendCode;
    }

    public int abendCode() {
        return abendCode;
    }
}
