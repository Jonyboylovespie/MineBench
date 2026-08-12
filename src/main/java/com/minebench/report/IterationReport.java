package com.minebench.report;

import com.minebench.benchmark.ValidityWarning;
import com.minebench.stats.FrameStatistics;
import com.minebench.stats.BottleneckMetrics;

import java.util.List;

public final class IterationReport {
    public String sceneId;
    public int iteration;
    public boolean valid;
    public List<ValidityWarning> warnings;
    public FrameStatistics statistics;
    public BottleneckMetrics bottleneck;

    public static IterationReport from(com.minebench.stats.IterationResult result) {
        IterationReport report = new IterationReport();
        report.sceneId = result.sceneId();
        report.iteration = result.iteration();
        report.valid = result.valid();
        report.warnings = result.warnings();
        report.statistics = result.statistics();
        report.bottleneck = result.bottleneck();
        return report;
    }
}
