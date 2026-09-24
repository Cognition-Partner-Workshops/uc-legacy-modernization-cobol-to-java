package carddemo;

import carddemo.programs.Abend;
import carddemo.programs.BatchProgram;
import carddemo.programs.Cbact01c;
import carddemo.programs.Cbact04c;
import carddemo.programs.Cbtrn02c;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * CLI entry point. Usage:
 *
 * <pre>
 * java -jar carddemo-batch.jar PROGRAM [--parm VALUE] [--now YYYY-MM-DD-HH.MM.SS.NNNNNN] --dd NAME=PATH ...
 * </pre>
 *
 * PROGRAM is one of CBACT01C, CBACT04C, CBTRN02C. Every DD the program's JCL allocates must be
 * given with {@code --dd}. Indexed (VSAM) datasets are fixed-record files that are read on
 * start-up and, when the program opens them I-O, rewritten in primary-key order on completion.
 * The exit status is the COBOL RETURN-CODE.
 */
public final class Main {
    private static final Map<String, BatchProgram> PROGRAMS = Map.of(
            "CBACT01C", new Cbact01c(),
            "CBACT04C", new Cbact04c(),
            "CBTRN02C", new Cbtrn02c());

    private Main() {
    }

    public static void main(String[] args) {
        int rc;
        try {
            rc = run(args, System.out);
        } catch (Abend e) {
            rc = Abend.ABEND_CODE;
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
        String programName = args[0].toUpperCase();
        BatchProgram program = PROGRAMS.get(programName);
        if (program == null) {
            throw new IllegalArgumentException("unknown program " + args[0]);
        }

        Map<String, Path> ddNames = new HashMap<>();
        String parm = null;
        String now = JobContext.DEFAULT_TIMESTAMP;
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--dd" -> {
                    String spec = argument(args, ++i, "--dd");
                    int eq = spec.indexOf('=');
                    if (eq <= 0) {
                        throw new IllegalArgumentException("--dd expects NAME=PATH, got '" + spec + "'");
                    }
                    ddNames.put(spec.substring(0, eq).toUpperCase(), Path.of(spec.substring(eq + 1)));
                }
                case "--parm" -> parm = argument(args, ++i, "--parm");
                case "--now" -> now = argument(args, ++i, "--now");
                default -> throw new IllegalArgumentException("unexpected argument '" + args[i] + "'");
            }
        }

        OutputStream sysout = new BufferedOutputStream(stdout);
        JobContext job = new JobContext(ddNames, parm, now, sysout);
        try {
            return program.run(job);
        } finally {
            try {
                sysout.flush();
            } catch (IOException e) {
                throw new IllegalStateException("cannot flush SYSOUT", e);
            }
        }
    }

    private static String argument(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException(option + " requires a value");
        }
        return args[index];
    }

    static String usage() {
        return """
                usage: PROGRAM [--parm VALUE] [--now TIMESTAMP] --dd NAME=PATH ...
                  CBACT01C --dd ACCTFILE=.. --dd OUTFILE=.. --dd ARRYFILE=.. --dd VBRCFILE=..
                  CBACT04C --parm YYYYMMDDHH --dd TCATBALF=.. --dd XREFFILE=.. --dd DISCGRP=.. --dd ACCTFILE=.. --dd TRANSACT=..
                  CBTRN02C --dd DALYTRAN=.. --dd XREFFILE=.. --dd ACCTFILE=.. --dd TCATBALF=.. --dd TRANFILE=.. --dd DALYREJS=..
                  --now defaults to the frozen harness time 2022-07-18-00.00.00.000000""";
    }
}
