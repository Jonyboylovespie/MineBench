package com.minebench.capture;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimitiveSampleBufferTest {
    @Test
    void firstTimestampIsOnlyAnAnchorAndIntervalsArePrimitive() {
        PrimitiveSampleBuffer buffer = new PrimitiveSampleBuffer(2, false);
        assertFalse(buffer.acceptTimestamp(100L, 0));
        assertTrue(buffer.acceptTimestamp(110L, 1));
        assertTrue(buffer.acceptTimestamp(125L, 2));
        assertEquals(2, buffer.storedCount());
        assertEquals(25L, buffer.measuredDurationNanos());
        assertEquals(10L, buffer.intervalsNanos()[0]);
        assertEquals(15L, buffer.intervalsNanos()[1]);
    }

    @Test
    void overflowKeepsAggregateCountAndMarksRawDataIncomplete() {
        PrimitiveSampleBuffer buffer = new PrimitiveSampleBuffer(1, false);
        buffer.acceptTimestamp(0L, 0);
        buffer.acceptTimestamp(10L, 0);
        buffer.acceptTimestamp(20L, 0);
        assertEquals(1, buffer.storedCount());
        assertEquals(2L, buffer.acceptedIntervalCount());
        assertTrue(buffer.overflowed());
    }
}
