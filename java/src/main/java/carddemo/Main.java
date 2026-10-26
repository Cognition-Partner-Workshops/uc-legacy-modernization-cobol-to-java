package carddemo;

import carddemo.programs.Abend;
import carddemo.programs.BatchProgram;
import carddemo.programs.Cbact03c;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * CLI entry point. Usage:
 *
 * <pre>
 * java -jar carddemo-batch.jar PROGRAM --dd NAME=PATH ...
 * </pre>
 *
 * Every DD the program's JCL allocates must be given with {@code --dd}. Indexed (VSAM KSDS)
 * datasets are fixed-record files that are loaded into key order on OPEN. The exit status is
 * the COBOL RETURN-CODE, or the CEE3ABD abend code (999) when the program abends.
 */
public final class Main {
    private static final Map<String, BatchProgram> PROGRAMS = Map.of(
            "CBACT03C", new Cbact03c());

    static final int SYSOUT_ERROR_RC = 12;

    private Main() {
    }

    public static void main(String[] args) {
        int rc;
        try {
            rc = run(args, System.out);
        } catch (Abend e) {
            rc = Abend.ABEND_CODE;
        } catch (SysoutException e) {
            System.err.println("error: " + e.getMessage());
            rc = SYSOUT_ERROR_RC;
        } catch (IllegalArgumentException e) {
            System.err.println("error: " + e.getMessage());
            System.err.println(usage());
            rc = 2;
        }
        System.exit(rc);
    }

    static int run(String[] args, PrintStream stdout) {
        if (args.length == 0) {
            throw new IllegalArgumentException("missing program name");
        }
        String programName = args[0].toUpperCase(Locale.ROOT);
        BatchProgram program = PROGRAMS.get(programName);
        if (program == null) {
            throw new IllegalArgumentException("unknown program " + args[0]);
        }

        Map<String, Path> ddNames = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            if (!"--dd".equals(args[i])) {
                throw new IllegalArgumentException("unexpected argument '" + args[i] + "'");
            }
            if (++i >= args.length) {
                throw new IllegalArgumentException("--dd requires a value");
            }
            String spec = args[i];
            int eq = spec.indexOf('=');
            if (eq <= 0) {
                throw new IllegalArgumentException("--dd expects NAME=PATH, got '" + spec + "'");
            }
            ddNames.put(spec.substring(0, eq).toUpperCase(Locale.ROOT), Path.of(spec.substring(eq + 1)));
        }

        OutputStream sysout = new BufferedOutputStream(stdout);
        JobContext job = new JobContext(ddNames, sysout);
        int rc;
        try {
            rc = program.run(job);
        } catch (RuntimeException e) {
            try {
                flushSysout(sysout, stdout);
            } catch (SysoutException flushError) {
                e.addSuppressed(flushError);
            }
            throw e;
        }
        flushSysout(sysout, stdout);
        return rc;
    }

    /** PrintStream swallows write errors, so a lost SYSOUT is detected through checkError(). */
    private static void flushSysout(OutputStream sysout, PrintStream stdout) {
        try {
            sysout.flush();
        } catch (IOException e) {
            throw new SysoutException(e);
        }
        if (stdout.checkError()) {
            throw new SysoutException(null);
        }
    }

    /** SYSOUT could not be written; the run fails with {@link #SYSOUT_ERROR_RC}. */
    static final class SysoutException extends RuntimeException {
        SysoutException(IOException cause) {
            super("cannot write SYSOUT", cause);
        }
    }

    static String usage() {
        return """
                usage: PROGRAM --dd NAME=PATH ...
                  CBACT03C --dd XREFFILE=<fixed 50-byte record file>""";
    }
}
