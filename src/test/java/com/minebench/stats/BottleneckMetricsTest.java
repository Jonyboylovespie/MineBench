package com.minebench.stats;

import com.minebench.benchmark.WorkloadTarget;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BottleneckMetricsTest {
    @Test
    void confirmsCpuTargetFromLowGpuTimingRatio() {
        BottleneckMetrics metrics = BottleneckMetrics.from(
                new double[]{42.0, 50.0, 58.0, Double.NaN}, WorkloadTarget.CPU);
        assertEquals("cpu-bound", metrics.classification);
        assertEquals(50.0, metrics.medianGpuUtilizationPercent);
        assertTrue(metrics.targetConfirmed);
    }

    @Test
    void confirmsGpuTargetFromHighGpuTimingRatio() {
        BottleneckMetrics metrics = BottleneckMetrics.from(
                new double[]{91.0, 96.0, 99.0}, WorkloadTarget.GPU);
        assertEquals("gpu-bound", metrics.classification);
        assertTrue(metrics.targetConfirmed);
    }

    @Test
    void reportsWhenRequestedTargetWasNotReached() {
        BottleneckMetrics metrics = BottleneckMetrics.from(
                new double[]{72.0, 80.0, 88.0}, WorkloadTarget.GPU);
        assertEquals("mixed", metrics.classification);
        assertFalse(metrics.targetConfirmed);
    }

    @Test
    void handlesUnavailableTimingWithoutInventingAClassification() {
        BottleneckMetrics metrics = BottleneckMetrics.from(
                new double[]{Double.NaN, 0.0}, WorkloadTarget.GPU);
        assertEquals("unavailable", metrics.classification);
        assertEquals(0, metrics.gpuTimingSamples);
        assertFalse(metrics.targetConfirmed);
    }
}
