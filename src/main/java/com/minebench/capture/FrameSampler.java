package com.minebench.capture;

import com.minebench.stats.FrameStatistics;

/** Thin hot-path facade around the fixed primitive buffer. */
public final class FrameSampler {
    private final PrimitiveSampleBuffer buffer;
    private boolean measuring;

    public FrameSampler(int capacity, boolean gpuTiming) {
        buffer = new PrimitiveSampleBuffer(capacity, gpuTiming);
    }

    public void beginMeasurement() {
        buffer.reset();
        measuring = true;
    }

    /** Called once at the chosen frame boundary. No object or collection is created here. */
    public void onPresentedFrame(long monotonicNanos, int eventFlags) {
        onPresentedFrame(monotonicNanos, eventFlags, Double.NaN);
    }

    public void onPresentedFrame(long monotonicNanos, int eventFlags, double gpuUtilizationPercent) {
        if (measuring) {
            buffer.acceptTimestamp(monotonicNanos, eventFlags, gpuUtilizationPercent);
        }
    }

    public SampledIteration endMeasurement() {
        measuring = false;
        PrimitiveSampleBuffer.Snapshot snapshot = buffer.snapshot();
        FrameStatistics statistics = FrameStatistics.from(
                snapshot.intervalsNanos(), snapshot.storedCount(), snapshot.measuredDurationNanos(),
                snapshot.acceptedIntervalCount(), !snapshot.overflowed());
        return new SampledIteration(snapshot, statistics);
    }

    public boolean measuring() {
        return measuring;
    }

    public PrimitiveSampleBuffer buffer() {
        return buffer;
    }

    public record SampledIteration(PrimitiveSampleBuffer.Snapshot samples, FrameStatistics statistics) {
    }
}
