package com.minebench.stats;

import com.minebench.benchmark.WorkloadTarget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** GPU timing-ratio evidence used to verify that a requested workload target was actually reached. */
public final class BottleneckMetrics {
    public String requestedTarget = WorkloadTarget.BALANCED.id();
    public String classification = "unavailable";
    public int gpuTimingSamples;
    public double medianGpuUtilizationPercent = Double.NaN;
    public double p10GpuUtilizationPercent = Double.NaN;
    public double p90GpuUtilizationPercent = Double.NaN;
    public boolean targetConfirmed;

    public static BottleneckMetrics from(double[] samples, WorkloadTarget target) {
        WorkloadTarget requested = target == null ? WorkloadTarget.BALANCED : target;
        BottleneckMetrics metrics = new BottleneckMetrics();
        metrics.requestedTarget = requested.id();
        if (samples == null || samples.length == 0) return metrics;

        double[] usable = Arrays.stream(samples)
                .filter(value -> Double.isFinite(value) && value > 0.0)
                .map(value -> Math.min(value, 200.0))
                .sorted()
                .toArray();
        metrics.gpuTimingSamples = usable.length;
        if (usable.length == 0) return metrics;

        metrics.medianGpuUtilizationPercent = percentile(usable, 0.50);
        metrics.p10GpuUtilizationPercent = percentile(usable, 0.10);
        metrics.p90GpuUtilizationPercent = percentile(usable, 0.90);
        metrics.classification = classify(metrics.medianGpuUtilizationPercent);
        metrics.targetConfirmed = switch (requested) {
            case CPU -> metrics.medianGpuUtilizationPercent <= 70.0;
            case GPU -> metrics.medianGpuUtilizationPercent >= 90.0;
            case BALANCED -> true;
        };
        return metrics;
    }

    public static BottleneckMetrics aggregate(List<BottleneckMetrics> inputs, WorkloadTarget target) {
        List<Double> medians = new ArrayList<>();
        if (inputs != null) {
            for (BottleneckMetrics input : inputs) {
                if (input != null && Double.isFinite(input.medianGpuUtilizationPercent)) {
                    medians.add(input.medianGpuUtilizationPercent);
                }
            }
        }
        double[] values = medians.stream().mapToDouble(Double::doubleValue).toArray();
        BottleneckMetrics result = from(values, target);
        result.gpuTimingSamples = inputs == null ? 0 : inputs.stream()
                .filter(java.util.Objects::nonNull).mapToInt(value -> value.gpuTimingSamples).sum();
        return result;
    }

    public static String classify(double medianGpuUtilizationPercent) {
        if (!Double.isFinite(medianGpuUtilizationPercent)) return "unavailable";
        if (medianGpuUtilizationPercent >= 90.0) return "gpu-bound";
        if (medianGpuUtilizationPercent <= 70.0) return "cpu-bound";
        return "mixed";
    }

    private static double percentile(double[] sorted, double percentile) {
        if (sorted.length == 1) return sorted[0];
        double index = percentile * (sorted.length - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        if (lower == upper) return sorted[lower];
        double fraction = index - lower;
        return sorted[lower] + (sorted[upper] - sorted[lower]) * fraction;
    }
}
