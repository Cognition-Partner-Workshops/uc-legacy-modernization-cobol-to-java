package com.carddemo.shared.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Java re-implementation of the batch wait utility {@code COBSWAIT}
 * ({@code app/cbl/COBSWAIT.cbl}) and its Assembler timer helper {@code MVSWAIT}
 * ({@code app/asm/MVSWAIT.asm}).
 *
 * <p>{@code COBSWAIT} reads a wait value in centiseconds (1/100th of a second)
 * from {@code SYSIN} and calls {@code MVSWAIT}, which issued an MVS interval
 * timer wait. This port replaces the OS timer with {@link Thread#sleep(long)}.</p>
 */
public final class BatchWait {

    /** Number of milliseconds in one centisecond (1/100th of a second). */
    private static final long MILLIS_PER_CENTISECOND = 10L;

    private BatchWait() {
    }

    /**
     * Waits for the given number of centiseconds, equivalent to the
     * {@code MVSWAIT-TIME} interval used by {@code COBSWAIT}/{@code MVSWAIT}.
     *
     * @param centiseconds the wait time in centiseconds (1/100th second); values
     *                     {@code <= 0} return immediately
     * @throws InterruptedException if the current thread is interrupted while
     *                              waiting
     */
    public static void waitCentiseconds(long centiseconds) throws InterruptedException {
        if (centiseconds <= 0) {
            return;
        }
        Thread.sleep(centiseconds * MILLIS_PER_CENTISECOND);
    }

    /**
     * Reads the wait time from the first command-line argument or, if none is
     * supplied, from standard input — mirroring how {@code COBSWAIT} performs
     * {@code ACCEPT PARM-VALUE FROM SYSIN}.
     *
     * @param args optional single argument: the wait time in centiseconds
     * @throws InterruptedException if interrupted while waiting
     */
    public static void main(String[] args) throws InterruptedException {
        String raw;
        if (args.length > 0) {
            raw = args[0];
        } else {
            raw = readFromStdin();
        }

        long centiseconds;
        try {
            centiseconds = Long.parseLong(raw == null ? "" : raw.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid wait value (centiseconds expected): " + raw);
            return;
        }

        waitCentiseconds(centiseconds);
    }

    private static String readFromStdin() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            return reader.readLine();
        } catch (Exception e) {
            return null;
        }
    }
}
