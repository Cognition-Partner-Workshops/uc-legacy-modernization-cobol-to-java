package carddemo.batch;

import carddemo.batch.programs.Cbact01c;
import carddemo.batch.programs.Cbact04c;
import carddemo.batch.programs.Cbtrn02c;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Job-step launcher.
 *
 * <pre>
 * java -jar carddemo-batch.jar PROGRAM [--dd DDNAME=path]... [--parm VALUE] [--now "yyyy-MM-dd HH:mm:ss"]
 * </pre>
 *
 * PROGRAM is CBACT01C, CBACT04C or CBTRN02C. {@code --dd} binds a DD name from the program's JCL
 * to a headerless fixed-length record file (KSDS data sets are updated in place, in key order).
 * {@code --parm} is the JCL PARM (CBACT04C needs the 10-character run date). {@code --now}
 * overrides the clock, which defaults to the harness's frozen 2022-07-18 00:00:00.
 * SYSOUT (DISPLAY) goes to stdout; the process exit code is the COBOL RETURN-CODE.
 */
public final class Main {
    private static final Map<String, Supplier<BatchProgram>> PROGRAMS = Map.of(
            "CBACT01C", Cbact01c::new,
            "CBACT04C", Cbact04c::new,
            "CBTRN02C", Cbtrn02c::new);
    private static final DateTimeFormatter NOW_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /** CEE3ABD U0999 surfaces as exit status 999 mod 256. */
    private static final int ABEND_EXIT_MASK = 0xFF;

    private Main() {
    }

    public static void main(String[] args) {
        System.exit(execute(args, new PrintStream(System.out, false, StandardCharsets.ISO_8859_1), System.err));
    }

    static int execute(String[] args, PrintStream out, PrintStream err) {
        if (args.length == 0 || !PROGRAMS.containsKey(args[0])) {
            err.println("usage: PROGRAM [--dd DDNAME=path]... [--parm VALUE] [--now \"yyyy-MM-dd HH:mm:ss\"]");
            err.println("programs: " + String.join(", ", PROGRAMS.keySet().stream().sorted().toList()));
            return 2;
        }
        Map<String, Path> dd = new HashMap<>();
        String parm = "";
        LocalDateTime now = BatchContext.FROZEN_NOW;
        for (int i = 1; i < args.length; i++) {
            String opt = args[i];
            if (i + 1 >= args.length) {
                err.println("missing value for " + opt);
                return 2;
            }
            String value = args[++i];
            switch (opt) {
                case "--dd" -> {
                    int eq = value.indexOf('=');
                    if (eq <= 0) {
                        err.println("--dd expects DDNAME=path, got " + value);
                        return 2;
                    }
                    dd.put(value.substring(0, eq), Path.of(value.substring(eq + 1)));
                }
                case "--parm" -> parm = value;
                case "--now" -> now = LocalDateTime.parse(value, NOW_FORMAT);
                default -> {
                    err.println("unknown option " + opt);
                    return 2;
                }
            }
        }
        BatchContext ctx = new BatchContext(dd, parm, now, out);
        try {
            return PROGRAMS.get(args[0]).get().run(ctx);
        } catch (Abend abend) {
            out.flush();
            err.println("CEE3ABD: " + abend.getMessage());
            return abend.code() & ABEND_EXIT_MASK;
        } catch (IOException | IllegalArgumentException e) {
            out.flush();
            err.println(args[0] + ": " + e.getMessage());
            return 12;
        } finally {
            out.flush();
        }
    }
}
