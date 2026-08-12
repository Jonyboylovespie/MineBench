package com.minebench.benchmark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

/** A deliberately small, machine-readable launch contract for managed mode. */
public final class ManagedRunPlan {
    public static final int CURRENT_SCHEMA_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int schemaVersion = CURRENT_SCHEMA_VERSION;
    public String label = "Managed run";
    public String mode = "standard";
    public String workloadTarget = "balanced";
    public int iterations = 5;
    public List<String> scenes = new ArrayList<>(List.of(
            "overworld_panorama", "overworld_flythrough", "nether_flythrough", "end_flythrough", "chunk_rebuild"));
    public long expectedWorldSeed = com.minebench.scene.SceneGenerator.SUITE_SEED;
    public String requiredWorldName = "MineBench Suite 1";
    public boolean requireDedicatedWorld = true;
    public boolean autoCreateWorld = true;
    public boolean autoStart;
    public boolean exitOnCompletion;
    public long orderSeed = 1L;
    public String statusFile = "config/minebench/managed-status.json";

    public static ManagedRunPlan fromJson(String json) {
        ManagedRunPlan plan = GSON.fromJson(json, ManagedRunPlan.class);
        if (plan == null) {
            throw new IllegalArgumentException("Managed plan is empty");
        }
        plan.sanitize();
        return plan;
    }

    public String toJson() {
        sanitize();
        return GSON.toJson(this);
    }

    public BenchmarkPlan toBenchmarkPlan() {
        BenchmarkPlan plan = BenchmarkPlan.standard(label);
        plan.mode = mode == null ? "standard" : mode;
        plan.workloadTarget = workloadTarget;
        plan.iterations = iterations;
        plan.scenes = new ArrayList<>(scenes);
        plan.orderSeed = orderSeed;
        plan.expectedWorldSeed = expectedWorldSeed;
        plan.requiredWorldName = requiredWorldName;
        plan.requireDedicatedWorld = requireDedicatedWorld;
        plan.sanitize();
        return plan;
    }

    public void sanitize() {
        schemaVersion = CURRENT_SCHEMA_VERSION;
        label = label == null || label.isBlank() ? "Managed run" : label.trim();
        mode = mode == null ? "standard" : mode.trim().toLowerCase(java.util.Locale.ROOT);
        workloadTarget = WorkloadTarget.parse(workloadTarget).id();
        iterations = Math.max(1, Math.min(iterations, 10_000));
        if (scenes == null || scenes.isEmpty()) {
            scenes = new ArrayList<>(List.of("overworld_panorama"));
        }
        requiredWorldName = requiredWorldName == null || requiredWorldName.isBlank()
                ? "MineBench Suite 1" : requiredWorldName.trim();
        statusFile = statusFile == null || statusFile.isBlank()
                ? "config/minebench/managed-status.json" : statusFile.trim();
    }
}
