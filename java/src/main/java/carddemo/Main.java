package carddemo;

import carddemo.programs.Abend;
import carddemo.programs.BatchProgram;
import carddemo.programs.Cbact01c;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Batch step launcher, the equivalent of {@code //STEP EXEC PGM=...}:
 *
 * <pre>
 * java -jar carddemo-batch.jar PROGRAM [--dd NAME=PATH]...
 * </pre>
 *
 * DDs can also be allocated through {@code DD_NAME=PATH} environment variables (the GnuCOBOL
 * convention); {@code --dd} wins. The process exit status is the program's RETURN-CODE, or the
 * abend code (999, seen by the shell as 999 mod 256 = 231) after a CEE3ABD.
 */
public final class Main {
    private static final Map<String, Supplier<BatchProgram>> PROGRAMS = Map.of(
            "CBACT01C", Cbact01c::new);

    private Main() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.getenv(), System.out, System.err));
    }

    static int run(String[] args, Map<String, String> env, PrintStream stdout, PrintStream stderr) {
        BatchProgram program;
        Map<String, Path> ddNames;
        try {
            program = program(args);
            ddNames = ddNames(args, env);
        } catch (IllegalArgumentException e) {
            stderr.println("error: " + e.getMessage());
            stderr.println(usage());
            return 2;
        }
        OutputStream sysout = new BufferedOutputStream(stdout);
        try {
            return program.run(new JobContext(ddNames, sysout));
        } catch (Abend e) {
            return e.abendCode();
        } finally {
            try {
                sysout.flush();
            } catch (IOException e) {
                throw new UncheckedIOException("cannot flush SYSOUT", e);
            }
        }
    }

    private static BatchProgram program(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("missing program name");
        }
        Supplier<BatchProgram> program = PROGRAMS.get(args[0].toUpperCase());
        if (program == null) {
            throw new IllegalArgumentException("unknown program " + args[0]);
        }
        return program.get();
    }

    private static Map<String, Path> ddNames(String[] args, Map<String, String> env) {
        Map<String, Path> ddNames = new HashMap<>();
        env.forEach((key, value) -> {
            if (key.startsWith("DD_") && key.length() > 3) {
                ddNames.put(key.substring(3).toUpperCase(), Path.of(value));
            }
        });
        for (int i = 1; i < args.length; i++) {
            if (!"--dd".equals(args[i]) || i + 1 >= args.length) {
                throw new IllegalArgumentException("unexpected argument '" + args[i] + "'");
            }
            String spec = args[++i];
            int eq = spec.indexOf('=');
            if (eq <= 0) {
                throw new IllegalArgumentException("--dd expects NAME=PATH, got '" + spec + "'");
            }
            ddNames.put(spec.substring(0, eq).toUpperCase(), Path.of(spec.substring(eq + 1)));
        }
        return ddNames;
    }

    static String usage() {
        return """
                usage: java -jar carddemo-batch.jar PROGRAM [--dd NAME=PATH]...
                  CBACT01C --dd ACCTFILE=.. --dd OUTFILE=.. --dd ARRYFILE=.. --dd VBRCFILE=..""";
    }
}
