package com.minebench.report;

import com.minebench.benchmark.BenchmarkTransition;
import com.minebench.benchmark.ValidityWarning;
import com.minebench.capture.RuntimeEventRecorder;
import com.minebench.environment.EnvironmentFingerprint;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BenchmarkReport {
    public int schemaVersion = ReportSchema.CURRENT_VERSION;
    public String minebenchVersion = "1.0.0";
    public String suiteVersion = "1";
    public String runId = "unavailable";
    public String label = "Unnamed";
    public String mode = "standard";
    public String settingsMode = "record-only";
    public String workloadTarget = "balanced";
    public double renderScale = 1.0;
    public String startedAt = "unavailable";
    public String completedAt = "unavailable";
    public EnvironmentFingerprint environment = EnvironmentFingerprint.unavailable();
    public List<SceneReport> scenes = new ArrayList<>();
    public List<BenchmarkTransition> phaseTransitions = new ArrayList<>();
    public List<RuntimeEventRecorder.RuntimeEvent> events = new ArrayList<>();
    public List<ValidityWarning> warnings = new ArrayList<>();
    public Map<String, Object> comparison = new LinkedHashMap<>();
    public boolean singleRunWarning;

    public void addScene(SceneReport scene) {
        scenes.add(scene);
    }
}
