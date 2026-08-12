package com.minebench.stats;

public record ConfidenceInterval(double lower, double upper, int resamples, boolean available) {
    public static ConfidenceInterval unavailable() {
        return new ConfidenceInterval(Double.NaN, Double.NaN, 0, false);
    }
}
