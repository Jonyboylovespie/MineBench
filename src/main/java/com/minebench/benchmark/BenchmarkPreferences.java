package com.minebench.benchmark;

public final class BenchmarkPreferences {
    public int schemaVersion = 1;
    public String mode = "record-only";
    public String defaultLabel = "Baseline";
    public String defaultWorkloadTarget = "balanced";
    public int defaultIterations = 5;
    public int warmupSeconds = 20;
    public int settlingSeconds = 5;
    public int maxPreparationSeconds = 180;
    public int maxWarmupSeconds = 180;
    public int frameCapacity = 900_000;
    public boolean autoStartManagedPlan;
    public boolean exitAfterManagedRun;
    public boolean showProgressOverlay;

    public void sanitize() {
        schemaVersion = 1;
        mode = mode == null ? "record-only" : mode;
        defaultLabel = defaultLabel == null || defaultLabel.isBlank() ? "Baseline" : defaultLabel.trim();
        defaultWorkloadTarget = WorkloadTarget.parse(defaultWorkloadTarget).id();
        defaultIterations = Math.max(1, Math.min(defaultIterations, 10_000));
        warmupSeconds = Math.max(0, Math.min(warmupSeconds, 900));
        settlingSeconds = Math.max(0, Math.min(settlingSeconds, 120));
        maxPreparationSeconds = Math.max(1, Math.min(maxPreparationSeconds, 3600));
        maxWarmupSeconds = Math.max(1, Math.min(maxWarmupSeconds, 3600));
        frameCapacity = Math.max(1_000, Math.min(frameCapacity, 10_000_000));
    }
}
