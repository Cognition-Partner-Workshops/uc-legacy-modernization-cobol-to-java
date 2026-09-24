package carddemo.batch;

/** CEE3ABD: the program requested a user abend with the given code. */
public final class Abend extends RuntimeException {
    private final int code;

    public Abend(int code) {
        super("USER ABEND U" + code);
        this.code = code;
    }

    public int code() {
        return code;
    }
}
