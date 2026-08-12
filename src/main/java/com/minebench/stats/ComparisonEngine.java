package com.minebench.stats;

import java.util.ArrayList;
import java.util.List;

public final class ComparisonEngine {
    private ComparisonEngine() {
    }

    public static ComparisonResult compare(
            String sceneId,
            String baselineLabel,
            RunAggregate baseline,
            String candidateLabel,
            RunAggregate candidate
    ) {
        List<String> warnings = new ArrayList<>();
        if (baseline == null || candidate == null) {
            warnings.add("One side of the comparison is missing");
            return new ComparisonResult(sceneId, baselineLabel, candidateLabel,
                    Double.NaN, Double.NaN, Double.NaN, Double.NaN, true, warnings);
        }
        double fpsDifference = candidate.medianAverageFps - baseline.medianAverageFps;
        double fpsPercent = percent(fpsDifference, baseline.medianAverageFps);
        double p99Difference = candidate.medianP99FrameTimeMillis - baseline.medianP99FrameTimeMillis;
        double p99Percent = percent(p99Difference, baseline.medianP99FrameTimeMillis);
        boolean smallerThanVariation = Double.isFinite(baseline.medianAbsoluteDeviationAverageFps)
                && Math.abs(fpsDifference) <= baseline.medianAbsoluteDeviationAverageFps;
        if (smallerThanVariation) {
            warnings.add("Observed average-FPS delta is no larger than baseline median absolute deviation");
        }
        if (baseline.validIterations < 5 || candidate.validIterations < 5) {
            warnings.add("Confidence estimates are limited because one side has fewer than five valid iterations");
        }
        return new ComparisonResult(sceneId, baselineLabel, candidateLabel,
                fpsDifference, fpsPercent, p99Difference, p99Percent, smallerThanVariation, warnings);
    }

    private static double percent(double difference, double baseline) {
        return baseline == 0.0 || !Double.isFinite(baseline) ? Double.NaN : difference / baseline * 100.0;
    }
}
