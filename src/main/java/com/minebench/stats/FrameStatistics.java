package com.minebench.stats;

/** Immutable post-run statistics with definitions matching METHODOLOGY.md. */
public final class FrameStatistics {
    public final int storedFrameCount;
    public final long acceptedFrameCount;
    public final long measuredDurationNanos;
    public final boolean rawSamplesComplete;
    public final double averageFps;
    public final double meanFrameTimeMillis;
    public final double medianFrameTimeMillis;
    public final double p90FrameTimeMillis;
    public final double p95FrameTimeMillis;
    public final double p99FrameTimeMillis;
    public final double p999FrameTimeMillis;
    public final double minimumFrameTimeMillis;
    public final double maximumFrameTimeMillis;
    public final double onePercentLowFps;
    public final double pointOnePercentLowFps;
    public final boolean pointOnePercentLowAvailable;
    public final long stuttersOver16Ms;
    public final long stuttersOver33Ms;
    public final long stuttersOver50Ms;
    public final long stuttersOver100Ms;
    public final long peakHeapBytes;
    public final long garbageCollections;
    public final long garbageCollectionPauseMillis;

    private FrameStatistics(
            int storedFrameCount,
            long acceptedFrameCount,
            long measuredDurationNanos,
            boolean rawSamplesComplete,
            double averageFps,
            double meanFrameTimeMillis,
            double medianFrameTimeMillis,
            double p90FrameTimeMillis,
            double p95FrameTimeMillis,
            double p99FrameTimeMillis,
            double p999FrameTimeMillis,
            double minimumFrameTimeMillis,
            double maximumFrameTimeMillis,
            double onePercentLowFps,
            double pointOnePercentLowFps,
            boolean pointOnePercentLowAvailable,
            long stuttersOver16Ms,
            long stuttersOver33Ms,
            long stuttersOver50Ms,
            long stuttersOver100Ms,
            long peakHeapBytes,
            long garbageCollections,
            long garbageCollectionPauseMillis
    ) {
        this.storedFrameCount = storedFrameCount;
        this.acceptedFrameCount = acceptedFrameCount;
        this.measuredDurationNanos = measuredDurationNanos;
        this.rawSamplesComplete = rawSamplesComplete;
        this.averageFps = averageFps;
        this.meanFrameTimeMillis = meanFrameTimeMillis;
        this.medianFrameTimeMillis = medianFrameTimeMillis;
        this.p90FrameTimeMillis = p90FrameTimeMillis;
        this.p95FrameTimeMillis = p95FrameTimeMillis;
        this.p99FrameTimeMillis = p99FrameTimeMillis;
        this.p999FrameTimeMillis = p999FrameTimeMillis;
        this.minimumFrameTimeMillis = minimumFrameTimeMillis;
        this.maximumFrameTimeMillis = maximumFrameTimeMillis;
        this.onePercentLowFps = onePercentLowFps;
        this.pointOnePercentLowFps = pointOnePercentLowFps;
        this.pointOnePercentLowAvailable = pointOnePercentLowAvailable;
        this.stuttersOver16Ms = stuttersOver16Ms;
        this.stuttersOver33Ms = stuttersOver33Ms;
        this.stuttersOver50Ms = stuttersOver50Ms;
        this.stuttersOver100Ms = stuttersOver100Ms;
        this.peakHeapBytes = peakHeapBytes;
        this.garbageCollections = garbageCollections;
        this.garbageCollectionPauseMillis = garbageCollectionPauseMillis;
    }

    public static FrameStatistics from(long[] intervalsNanos, int count, long durationNanos) {
        return from(intervalsNanos, count, durationNanos, count, true);
    }

    public static FrameStatistics from(
            long[] intervalsNanos,
            int count,
            long durationNanos,
            long acceptedCount,
            boolean rawSamplesComplete
    ) {
        long[] sorted = Percentiles.sortedCopy(intervalsNanos, count);
        int stored = sorted.length;
        if (stored == 0 || durationNanos <= 0L) {
            return empty(stored, acceptedCount, durationNanos, rawSamplesComplete);
        }

        double totalNanos = 0.0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long over16 = 0L;
        long over33 = 0L;
        long over50 = 0L;
        long over100 = 0L;
        for (long interval : sorted) {
            totalNanos += interval;
            min = Math.min(min, interval);
            max = Math.max(max, interval);
            if (interval > 16_670_000L) over16++;
            if (interval > 33_330_000L) over33++;
            if (interval > 50_000_000L) over50++;
            if (interval > 100_000_000L) over100++;
        }
        double seconds = durationNanos / 1_000_000_000.0;
        double meanNanos = totalNanos / stored;
        double slowOnePercentNanos = Percentiles.slowestAverage(sorted, 0.01);
        boolean pointOneAvailable = stored >= 1_000;
        double slowPointOnePercentNanos = pointOneAvailable
                ? Percentiles.slowestAverage(sorted, 0.001) : Double.NaN;
        return new FrameStatistics(
                stored,
                acceptedCount,
                durationNanos,
                rawSamplesComplete,
                acceptedCount / seconds,
                meanNanos / 1_000_000.0,
                Percentiles.interpolated(sorted, 0.50) / 1_000_000.0,
                Percentiles.interpolated(sorted, 0.90) / 1_000_000.0,
                Percentiles.interpolated(sorted, 0.95) / 1_000_000.0,
                Percentiles.interpolated(sorted, 0.99) / 1_000_000.0,
                Percentiles.interpolated(sorted, 0.999) / 1_000_000.0,
                min / 1_000_000.0,
                max / 1_000_000.0,
                1_000.0 / (slowOnePercentNanos / 1_000_000.0),
                pointOneAvailable ? 1_000.0 / (slowPointOnePercentNanos / 1_000_000.0) : Double.NaN,
                pointOneAvailable,
                over16,
                over33,
                over50,
                over100,
                0L,
                0L,
                0L
        );
    }

    public FrameStatistics withRuntimeCounters(long peakHeapBytes, long garbageCollections, long pauseMillis) {
        return new FrameStatistics(
                storedFrameCount, acceptedFrameCount, measuredDurationNanos, rawSamplesComplete,
                averageFps, meanFrameTimeMillis, medianFrameTimeMillis, p90FrameTimeMillis, p95FrameTimeMillis,
                p99FrameTimeMillis, p999FrameTimeMillis, minimumFrameTimeMillis, maximumFrameTimeMillis,
                onePercentLowFps, pointOnePercentLowFps, pointOnePercentLowAvailable,
                stuttersOver16Ms, stuttersOver33Ms, stuttersOver50Ms, stuttersOver100Ms,
                peakHeapBytes, garbageCollections, pauseMillis
        );
    }

    private static FrameStatistics empty(int stored, long accepted, long duration, boolean complete) {
        return new FrameStatistics(
                stored, accepted, duration, complete,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, false,
                0L, 0L, 0L, 0L, 0L, 0L, 0L
        );
    }
}
