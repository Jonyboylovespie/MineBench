package com.minebench.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrameStatisticsTest {
    @Test
    void calculatesMeanMedianPercentilesAndOnePercentLow() {
        long[] intervals = {10_000_000L, 20_000_000L, 30_000_000L, 40_000_000L};
        FrameStatistics statistics = FrameStatistics.from(intervals, intervals.length, 100_000_000L);

        assertEquals(40.0, statistics.averageFps, 1e-9);
        assertEquals(25.0, statistics.meanFrameTimeMillis, 1e-9);
        assertEquals(25.0, statistics.medianFrameTimeMillis, 1e-9);
        assertEquals(38.5, statistics.p95FrameTimeMillis, 1e-9);
        assertEquals(25.0, statistics.onePercentLowFps, 1e-9);
        assertFalse(statistics.pointOnePercentLowAvailable);
        assertEquals(1L, statistics.stuttersOver33Ms);
    }

    @Test
    void oneTenthPercentLowRequiresEnoughSamples() {
        long[] intervals = new long[1_000];
        java.util.Arrays.fill(intervals, 16_000_000L);
        FrameStatistics statistics = FrameStatistics.from(intervals, intervals.length, 16_000_000_000L);

        assertTrue(statistics.pointOnePercentLowAvailable);
        assertEquals(62.5, statistics.pointOnePercentLowFps, 1e-9);
    }

    @Test
    void zeroAndEmptySamplesAreExplicitlyUnavailable() {
        FrameStatistics statistics = FrameStatistics.from(new long[0], 0, 0L);
        assertTrue(Double.isNaN(statistics.averageFps));
        assertEquals(0, statistics.storedFrameCount);
    }
}
