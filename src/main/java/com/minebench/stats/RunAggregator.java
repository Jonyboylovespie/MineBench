package com.minebench.stats;

import java.util.ArrayList;
import java.util.List;

public final class RunAggregator {
    private final String sceneId;
    private final List<IterationResult> iterations = new ArrayList<>();

    public RunAggregator(String sceneId) {
        this.sceneId = sceneId;
    }

    public void add(IterationResult result) {
        if (result != null) {
            iterations.add(result);
        }
    }

    public List<IterationResult> iterations() {
        return List.copyOf(iterations);
    }

    public RunAggregate aggregate() {
        List<IterationResult> valid = iterations.stream().filter(IterationResult::valid).toList();
        double[] fps = valid.stream().mapToDouble(result -> result.statistics().averageFps).toArray();
        double[] onePercent = valid.stream().mapToDouble(result -> result.statistics().onePercentLowFps).toArray();
        double[] p99 = valid.stream().mapToDouble(result -> result.statistics().p99FrameTimeMillis).toArray();
        double medianFps = Percentiles.median(fps);
        double mad = medianAbsoluteDeviation(fps, medianFps);
        double mean = mean(fps);
        double cv = mean == 0.0 || Double.isNaN(mean) ? Double.NaN : standardDeviation(fps, mean) / mean;
        ConfidenceInterval confidence = BootstrapConfidenceInterval.mean(fps, 10_000, 0x4D494E4542454E43L, 0.95);
        return new RunAggregate(
                sceneId,
                iterations.size(),
                valid.size(),
                (int) iterations.stream().filter(result -> !result.valid()).count(),
                0,
                medianFps,
                Percentiles.median(onePercent),
                Percentiles.median(p99),
                min(fps),
                max(fps),
                mad,
                cv,
                confidence
        );
    }

    private static double medianAbsoluteDeviation(double[] values, double median) {
        if (values.length == 0 || Double.isNaN(median)) {
            return Double.NaN;
        }
        double[] deviations = new double[values.length];
        for (int index = 0; index < values.length; index++) {
            deviations[index] = Math.abs(values[index] - median);
        }
        return Percentiles.median(deviations);
    }

    private static double mean(double[] values) {
        if (values.length == 0) return Double.NaN;
        double total = 0.0;
        for (double value : values) total += value;
        return total / values.length;
    }

    private static double standardDeviation(double[] values, double mean) {
        if (values.length < 2) return 0.0;
        double total = 0.0;
        for (double value : values) total += (value - mean) * (value - mean);
        return Math.sqrt(total / values.length);
    }

    private static double min(double[] values) {
        if (values.length == 0) return Double.NaN;
        double result = Double.POSITIVE_INFINITY;
        for (double value : values) result = Math.min(result, value);
        return result;
    }

    private static double max(double[] values) {
        if (values.length == 0) return Double.NaN;
        double result = Double.NEGATIVE_INFINITY;
        for (double value : values) result = Math.max(result, value);
        return result;
    }
}
