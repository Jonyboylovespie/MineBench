package com.minebench.stats;

import com.minebench.benchmark.ValidityWarning;
import com.minebench.capture.PrimitiveSampleBuffer;

import java.util.List;

public record IterationResult(
        String sceneId,
        int iteration,
        boolean valid,
        List<ValidityWarning> warnings,
        FrameStatistics statistics,
        BottleneckMetrics bottleneck,
        PrimitiveSampleBuffer.Snapshot samples
) {
    public IterationResult {
        warnings = List.copyOf(warnings == null ? List.of() : warnings);
    }
}
