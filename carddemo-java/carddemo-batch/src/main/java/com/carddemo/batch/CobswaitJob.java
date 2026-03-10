package com.carddemo.batch;

/**
 * Migrated from COBOL program COBSWAIT.cbl.
 * <p>
 * Utility program to pause execution for a specified number of centiseconds
 * (hundredths of a second). The COBOL version calls MVSWAIT; this Java version
 * uses {@link Thread#sleep(long)}.
 */
public class CobswaitJob {

    /**
     * Wait for the specified duration.
     *
     * @param centiseconds wait time in centiseconds (1/100 of a second)
     */
    public static void wait(int centiseconds) {
        long millis = centiseconds * 10L;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: CobswaitJob <centiseconds>");
            System.exit(1);
        }
        int centiseconds = Integer.parseInt(args[0].strip());
        wait(centiseconds);
    }
}
