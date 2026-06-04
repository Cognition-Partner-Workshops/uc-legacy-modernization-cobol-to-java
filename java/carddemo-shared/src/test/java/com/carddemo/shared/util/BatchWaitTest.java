package com.carddemo.shared.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link BatchWait} (port of {@code COBSWAIT}/{@code MVSWAIT}). */
class BatchWaitTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void waitsAtLeastTheRequestedDuration() throws InterruptedException {
        long centiseconds = 25; // 250 ms
        long start = System.nanoTime();
        BatchWait.waitCentiseconds(centiseconds);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis >= 250,
                () -> "expected >= 250ms, was " + elapsedMillis + "ms");
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void zeroReturnsImmediately() throws InterruptedException {
        long start = System.nanoTime();
        BatchWait.waitCentiseconds(0);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis < 100,
                () -> "expected near-immediate return, was " + elapsedMillis + "ms");
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void negativeReturnsImmediately() throws InterruptedException {
        BatchWait.waitCentiseconds(-50);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void mainAcceptsArgument() throws InterruptedException {
        BatchWait.main(new String[]{"10"});
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void mainHandlesNonNumericArgumentGracefully() throws InterruptedException {
        BatchWait.main(new String[]{"abc"});
    }
}
