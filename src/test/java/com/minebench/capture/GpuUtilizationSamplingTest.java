package com.minebench.capture;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GpuUtilizationSamplingTest {
    @Test
    void storesGpuTimingRatioBesideAcceptedFrameInterval() {
        PrimitiveSampleBuffer buffer = new PrimitiveSampleBuffer(4, true);
        buffer.acceptTimestamp(1_000L, 0, 12.0);
        buffer.acceptTimestamp(2_000L, 0, 87.5);
        buffer.acceptTimestamp(4_500L, 0, 91.0);

        PrimitiveSampleBuffer.Snapshot snapshot = buffer.snapshot();
        assertEquals(2, snapshot.storedCount());
        assertArrayEquals(new long[]{1_000L, 2_500L}, snapshot.intervalsNanos());
        assertArrayEquals(new long[]{875L, 2_275L}, snapshot.gpuNanos());
        assertArrayEquals(new double[]{87.5, 91.0}, snapshot.gpuUtilizationPercent());
    }
}
