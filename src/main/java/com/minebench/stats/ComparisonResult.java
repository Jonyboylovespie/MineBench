package com.minebench.stats;

import java.util.List;

public final class ComparisonResult {
    public final String sceneId;
    public final String baselineLabel;
    public final String candidateLabel;
    public final double averageFpsAbsoluteDifference;
    public final double averageFpsPercentChange;
    public final double p99FrameTimeAbsoluteDifferenceMillis;
    public final double p99FrameTimePercentChange;
    public final boolean deltaSmallerThanBaselineVariation;
    public final List<String> warnings;

    public ComparisonResult(
            String sceneId,
            String baselineLabel,
            String candidateLabel,
            double averageFpsAbsoluteDifference,
            double averageFpsPercentChange,
            double p99FrameTimeAbsoluteDifferenceMillis,
            double p99FrameTimePercentChange,
            boolean deltaSmallerThanBaselineVariation,
            List<String> warnings
    ) {
        this.sceneId = sceneId;
        this.baselineLabel = baselineLabel;
        this.candidateLabel = candidateLabel;
        this.averageFpsAbsoluteDifference = averageFpsAbsoluteDifference;
        this.averageFpsPercentChange = averageFpsPercentChange;
        this.p99FrameTimeAbsoluteDifferenceMillis = p99FrameTimeAbsoluteDifferenceMillis;
        this.p99FrameTimePercentChange = p99FrameTimePercentChange;
        this.deltaSmallerThanBaselineVariation = deltaSmallerThanBaselineVariation;
        this.warnings = List.copyOf(warnings == null ? List.of() : warnings);
    }
}
