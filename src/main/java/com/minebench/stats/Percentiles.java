package com.minebench.stats;

import java.util.Arrays;

public final class Percentiles {
    private Percentiles() {
    }

    public static long[] sortedCopy(long[] values, int count) {
        if (values == null || count <= 0) {
            return new long[0];
        }
        int bounded = Math.min(count, values.length);
        long[] copy = Arrays.copyOf(values, bounded);
        Arrays.sort(copy);
        return copy;
    }

    /** Linear interpolation at p*(n-1), with p in [0, 1]. */
    public static double interpolated(long[] sorted, double p) {
        if (sorted == null || sorted.length == 0) {
            return Double.NaN;
        }
        if (sorted.length == 1) {
            return sorted[0];
        }
        double position = Math.max(0.0, Math.min(1.0, p)) * (sorted.length - 1);
        int lower = (int) Math.floor(position);
        int upper = Math.min(sorted.length - 1, lower + 1);
        double fraction = position - lower;
        return sorted[lower] + (sorted[upper] - sorted[lower]) * fraction;
    }

    public static double median(double[] values) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        }
        double[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);
        int middle = sorted.length / 2;
        return (sorted.length & 1) == 1 ? sorted[middle] : (sorted[middle - 1] + sorted[middle]) / 2.0;
    }

    public static int slowestCount(int sampleCount, double fraction) {
        if (sampleCount <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(sampleCount * fraction));
    }

    public static double slowestAverage(long[] sorted, double fraction) {
        if (sorted == null || sorted.length == 0) {
            return Double.NaN;
        }
        int count = slowestCount(sorted.length, fraction);
        long total = 0L;
        double preciseTotal = 0.0;
        for (int index = sorted.length - count; index < sorted.length; index++) {
            preciseTotal += sorted[index];
        }
        return preciseTotal / count;
    }
}
