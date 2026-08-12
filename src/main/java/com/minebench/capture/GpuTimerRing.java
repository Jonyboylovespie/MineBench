package com.minebench.capture;

/**
 * Fixed-size non-blocking result ring for optional GPU timer adapters. OpenGL calls are supplied by the client edge;
 * this class never waits for a query result and never grows while measuring.
 */
public final class GpuTimerRing {
    private final long[] queryHandles;
    private final long[] resultNanos;
    private final boolean[] inUse;
    private final boolean[] ready;
    private int writeIndex;
    private int readIndex;

    public GpuTimerRing(int capacity) {
        if (capacity < 2) throw new IllegalArgumentException("GPU timer ring needs at least two entries");
        queryHandles = new long[capacity];
        resultNanos = new long[capacity];
        inUse = new boolean[capacity];
        ready = new boolean[capacity];
    }

    public boolean reserve(long queryHandle) {
        if (inUse[writeIndex]) return false;
        queryHandles[writeIndex] = queryHandle;
        inUse[writeIndex] = true;
        ready[writeIndex] = false;
        writeIndex = (writeIndex + 1) % queryHandles.length;
        return true;
    }

    /** Called only after an asynchronous availability check says the result is ready. */
    public boolean publishIfAvailable(long queryHandle, long elapsedNanos) {
        for (int index = 0; index < queryHandles.length; index++) {
            if (queryHandles[index] == queryHandle && inUse[index] && !ready[index]) {
                resultNanos[index] = elapsedNanos;
                ready[index] = true;
                return true;
            }
        }
        return false;
    }

    /** Returns zero when no result is ready; it never blocks. */
    public long poll(long queryHandle) {
        for (int index = 0; index < queryHandles.length; index++) {
            if (queryHandles[index] == queryHandle && inUse[index] && ready[index]) {
                long result = resultNanos[index];
                inUse[index] = false;
                ready[index] = false;
                if (index == readIndex) readIndex = (readIndex + 1) % queryHandles.length;
                return result;
            }
        }
        return 0L;
    }

    public int capacity() {
        return queryHandles.length;
    }
}
