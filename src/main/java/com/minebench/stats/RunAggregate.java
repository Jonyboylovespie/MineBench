package com.minebench.stats;

public final class RunAggregate {
    public final String sceneId;
    public final int totalIterations;
    public final int validIterations;
    public final int invalidIterations;
    public final int abortedIterations;
    public final double medianAverageFps;
    public final double medianOnePercentLowFps;
    public final double medianP99FrameTimeMillis;
    public final double minimumAverageFps;
    public final double maximumAverageFps;
    public final double medianAbsoluteDeviationAverageFps;
    public final double coefficientOfVariationAverageFps;
    public final ConfidenceInterval averageFpsConfidenceInterval;

    public RunAggregate(
            String sceneId,
            int totalIterations,
            int validIterations,
            int invalidIterations,
            int abortedIterations,
            double medianAverageFps,
            double medianOnePercentLowFps,
            double medianP99FrameTimeMillis,
            double minimumAverageFps,
            double maximumAverageFps,
            double medianAbsoluteDeviationAverageFps,
            double coefficientOfVariationAverageFps,
            ConfidenceInterval averageFpsConfidenceInterval
    ) {
        this.sceneId = sceneId;
        this.totalIterations = totalIterations;
        this.validIterations = validIterations;
        this.invalidIterations = invalidIterations;
        this.abortedIterations = abortedIterations;
        this.medianAverageFps = medianAverageFps;
        this.medianOnePercentLowFps = medianOnePercentLowFps;
        this.medianP99FrameTimeMillis = medianP99FrameTimeMillis;
        this.minimumAverageFps = minimumAverageFps;
        this.maximumAverageFps = maximumAverageFps;
        this.medianAbsoluteDeviationAverageFps = medianAbsoluteDeviationAverageFps;
        this.coefficientOfVariationAverageFps = coefficientOfVariationAverageFps;
        this.averageFpsConfidenceInterval = averageFpsConfidenceInterval;
    }
}
