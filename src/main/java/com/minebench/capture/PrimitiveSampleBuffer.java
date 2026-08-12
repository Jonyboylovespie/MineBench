package com.minebench.capture;

import java.util.Arrays;

/**
 * Fixed-capacity primitive storage. The acceptTimestamp method is intentionally allocation-free and does no I/O,
 * sorting, formatting, or collection work.
 */
public final class PrimitiveSampleBuffer {
    private final long[] intervalsNanos;
    private final long[] gpuNanos;
    private final double[] gpuUtilizationPercent;
    private final int[] eventFlags;
    private int storedCount;
    private long acceptedIntervalCount;
    private long firstTimestampNanos;
    private long previousTimestampNanos;
    private long lastTimestampNanos;
    private boolean anchored;
    private boolean overflowed;

    public PrimitiveSampleBuffer(int capacity, boolean gpuTiming) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Sample capacity must be positive");
        }
        intervalsNanos = new long[capacity];
        gpuNanos = gpuTiming ? new long[capacity] : null;
        gpuUtilizationPercent = new double[capacity];
        Arrays.fill(gpuUtilizationPercent, Double.NaN);
        eventFlags = new int[capacity];
    }

    /** Returns true when the timestamp became a stored interval. */
    public boolean acceptTimestamp(long timestampNanos, int flags) {
        return acceptTimestamp(timestampNanos, flags, Double.NaN);
    }

    /** Returns true when the timestamp became a stored interval. */
    public boolean acceptTimestamp(long timestampNanos, int flags, double gpuUtilization) {
        if (!anchored) {
            anchored = true;
            firstTimestampNanos = timestampNanos;
            previousTimestampNanos = timestampNanos;
            lastTimestampNanos = timestampNanos;
            return false;
        }
        long interval = timestampNanos - previousTimestampNanos;
        previousTimestampNanos = timestampNanos;
        lastTimestampNanos = timestampNanos;
        acceptedIntervalCount++;
        if (storedCount >= intervalsNanos.length) {
            overflowed = true;
            return false;
        }
        intervalsNanos[storedCount] = interval;
        gpuUtilizationPercent[storedCount] = gpuUtilization;
        if (gpuNanos != null && Double.isFinite(gpuUtilization) && gpuUtilization > 0.0) {
            // Minecraft reports GPU time as a percentage of this same presented-frame interval.
            gpuNanos[storedCount] = Math.max(0L, Math.round(interval * gpuUtilization / 100.0));
        }
        eventFlags[storedCount] = flags;
        storedCount++;
        return true;
    }

    public void setGpuNanosForLastStored(long gpuNanosValue) {
        if (gpuNanos != null && storedCount > 0) {
            gpuNanos[storedCount - 1] = gpuNanosValue;
        }
    }

    public void reset() {
        storedCount = 0;
        acceptedIntervalCount = 0L;
        firstTimestampNanos = 0L;
        previousTimestampNanos = 0L;
        lastTimestampNanos = 0L;
        anchored = false;
        overflowed = false;
        if (gpuNanos != null) Arrays.fill(gpuNanos, 0L);
        Arrays.fill(gpuUtilizationPercent, Double.NaN);
    }

    public int capacity() {
        return intervalsNanos.length;
    }

    public int storedCount() {
        return storedCount;
    }

    public long acceptedIntervalCount() {
        return acceptedIntervalCount;
    }

    public long measuredDurationNanos() {
        return anchored ? Math.max(0L, lastTimestampNanos - firstTimestampNanos) : 0L;
    }

    public boolean overflowed() {
        return overflowed;
    }

    public long[] intervalsNanos() {
        return intervalsNanos;
    }

    public long[] gpuNanos() {
        return gpuNanos;
    }

    public int[] eventFlags() {
        return eventFlags;
    }

    public double[] gpuUtilizationPercent() {
        return gpuUtilizationPercent;
    }

    public Snapshot snapshot() {
        return new Snapshot(
                Arrays.copyOf(intervalsNanos, storedCount),
                gpuNanos == null ? null : Arrays.copyOf(gpuNanos, storedCount),
                Arrays.copyOf(gpuUtilizationPercent, storedCount),
                Arrays.copyOf(eventFlags, storedCount),
                storedCount,
                acceptedIntervalCount,
                measuredDurationNanos(),
                overflowed
        );
    }

    public record Snapshot(
            long[] intervalsNanos,
            long[] gpuNanos,
            double[] gpuUtilizationPercent,
            int[] eventFlags,
            int storedCount,
            long acceptedIntervalCount,
            long measuredDurationNanos,
            boolean overflowed
    ) {
        public Snapshot {
            intervalsNanos = intervalsNanos == null ? new long[0] : intervalsNanos;
            gpuUtilizationPercent = gpuUtilizationPercent == null ? new double[0] : gpuUtilizationPercent;
            eventFlags = eventFlags == null ? new int[0] : eventFlags;
        }
    }
}
