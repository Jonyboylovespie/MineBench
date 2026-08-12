package com.minebench.stats;

import java.util.Arrays;
import java.util.Random;

/** Deterministic percentile bootstrap for repeated-iteration metrics. */
public final class BootstrapConfidenceInterval {
    private BootstrapConfidenceInterval() {
    }

    public static ConfidenceInterval mean(double[] values, int resamples, long seed, double confidence) {
        if (values == null || values.length < 5 || resamples < 1) {
            return ConfidenceInterval.unavailable();
        }
        int repeats = Math.max(100, resamples);
        double[] distribution = new double[repeats];
        Random random = new Random(seed);
        for (int repeat = 0; repeat < repeats; repeat++) {
            double total = 0.0;
            for (int sample = 0; sample < values.length; sample++) {
                total += values[random.nextInt(values.length)];
            }
            distribution[repeat] = total / values.length;
        }
        Arrays.sort(distribution);
        double alpha = Math.max(0.0, Math.min(0.99, 1.0 - confidence));
        double lower = numericPercentile(distribution, alpha / 2.0);
        double upper = numericPercentile(distribution, 1.0 - alpha / 2.0);
        return new ConfidenceInterval(lower, upper, repeats, true);
    }

    public static double numericPercentile(double[] sorted, double p) {
        if (sorted.length == 0) return Double.NaN;
        double position = Math.max(0.0, Math.min(1.0, p)) * (sorted.length - 1);
        int lower = (int) Math.floor(position);
        int upper = Math.min(sorted.length - 1, lower + 1);
        return sorted[lower] + (sorted[upper] - sorted[lower]) * (position - lower);
    }
}
