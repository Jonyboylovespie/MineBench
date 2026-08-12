package com.minebench.report;

import com.minebench.stats.RunAggregate;
import com.minebench.stats.BottleneckMetrics;

import java.util.List;

public final class SceneReport {
    public String sceneId;
    public String displayName;
    public int durationSeconds;
    public String workloadDescription;
    public String dimension;
    public String anchor;
    public int readinessRadiusChunks;
    public String cameraResource;
    public String routeHash = "unavailable";
    public String sceneContentHash = "unavailable";
    public boolean geometryRebuild;
    public List<IterationReport> iterations;
    public RunAggregate aggregate;
    public BottleneckMetrics bottleneck;
}
