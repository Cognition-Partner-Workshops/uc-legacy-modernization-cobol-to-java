package com.cardemo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wait Utility - converted from COBOL program COBSWAIT.cbl (CSSWAIT)
 * 
 * Original: COBOL subprogram that provides a timed wait facility.
 * Used by batch programs to introduce delays during processing.
 * 
 * The COBOL version used:
 *   CALL 'COBSWAIT' USING WS-WAIT-SECONDS
 * 
 * In Java, Thread.sleep() provides equivalent functionality.
 */
public final class WaitUtil {

    private static final Logger log = LoggerFactory.getLogger(WaitUtil.class);

    private WaitUtil() {}

    /**
     * Wait for the specified number of seconds.
     * Equivalent to COBSWAIT subprogram call.
     *
     * @param seconds number of seconds to wait
     */
    public static void waitSeconds(int seconds) {
        if (seconds <= 0) {
            return;
        }
        try {
            log.debug("Waiting for {} seconds...", seconds);
            Thread.sleep(seconds * 1000L);
            log.debug("Wait complete.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Wait interrupted after requesting {} seconds", seconds);
        }
    }

    /**
     * Wait for the specified number of milliseconds.
     *
     * @param millis number of milliseconds to wait
     */
    public static void waitMillis(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Wait interrupted after requesting {} ms", millis);
        }
    }
}
