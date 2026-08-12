package com.minebench.benchmark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minebench.scene.SceneDefinitions;
import com.minebench.scene.SceneDescriptor;

import java.util.ArrayList;
import java.util.List;

/** Versioned user-facing plan. Gson ignores unknown future fields by design. */
public final class BenchmarkPlan {
    public static final int CURRENT_SCHEMA_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int schemaVersion = CURRENT_SCHEMA_VERSION;
    public String suiteVersion = "1";
    public String mode = "standard";
    public String label = "Baseline";
    public String workloadTarget = "balanced";
    public int iterations = 5;
    public String settingsMode = "record-only";
    public long orderSeed = 1L;
    public int warmupSeconds = 20;
    public int settlingSeconds = 5;
    public int maxPreparationSeconds = 180;
    public int maxWarmupSeconds = 180;
    public int frameCapacity = 900_000;
    public long expectedWorldSeed = com.minebench.scene.SceneGenerator.SUITE_SEED;
    public String requiredWorldName = "MineBench Suite 1";
    public boolean requireDedicatedWorld = true;
    public List<String> scenes = new ArrayList<>();
    public boolean acknowledgeEnvironmentDifferences;

    public BenchmarkPlan() {
        scenes.add("overworld_panorama");
        scenes.add("overworld_flythrough");
        scenes.add("nether_flythrough");
        scenes.add("end_flythrough");
        scenes.add("chunk_rebuild");
    }

    public static BenchmarkPlan quick() {
        BenchmarkPlan plan = new BenchmarkPlan();
        plan.mode = "quick";
        plan.label = "Quick benchmark";
        plan.iterations = 1;
        plan.warmupSeconds = 20;
        plan.scenes = new ArrayList<>(List.of("overworld_panorama", "overworld_flythrough"));
        return plan;
    }

    public static BenchmarkPlan standard(String label) {
        BenchmarkPlan plan = new BenchmarkPlan();
        if (label != null && !label.isBlank()) {
            plan.label = label.trim();
        }
        return plan;
    }

    public static BenchmarkPlan extended(String label) {
        BenchmarkPlan plan = standard(label);
        plan.mode = "extended";
        plan.iterations = 10;
        plan.warmupSeconds = 60;
        plan.scenes = new ArrayList<>();
        for (SceneDescriptor scene : SceneDefinitions.all()) {
            plan.scenes.add(scene.id());
        }
        return plan;
    }

    public static BenchmarkPlan fromJson(String json) {
        BenchmarkPlan plan = GSON.fromJson(json, BenchmarkPlan.class);
        if (plan == null) {
            throw new IllegalArgumentException("Benchmark plan is empty");
        }
        plan.sanitize();
        return plan;
    }

    public String toJson() {
        sanitize();
        return GSON.toJson(this);
    }

    public BenchmarkMode benchmarkMode() {
        return BenchmarkMode.parse(mode);
    }

    public SettingsMode settingsModeValue() {
        return SettingsMode.parse(settingsMode);
    }

    public WorkloadTarget workloadTargetValue() {
        return WorkloadTarget.parse(workloadTarget);
    }

    public BenchmarkPlan withWorkloadTarget(WorkloadTarget target) {
        workloadTarget = (target == null ? WorkloadTarget.BALANCED : target).id();
        return this;
    }

    public List<SceneDescriptor> sceneDescriptors() {
        List<SceneDescriptor> descriptors = new ArrayList<>();
        for (String id : scenes) {
            SceneDefinitions.find(id).ifPresent(descriptors::add);
        }
        return List.copyOf(descriptors);
    }

    public void sanitize() {
        schemaVersion = CURRENT_SCHEMA_VERSION;
        suiteVersion = suiteVersion == null || suiteVersion.isBlank() ? "1" : suiteVersion.trim();
        label = label == null || label.isBlank() ? "Unnamed" : label.trim();
        workloadTarget = workloadTargetValue().id();
        iterations = Math.max(1, Math.min(iterations, 10_000));
        warmupSeconds = Math.max(0, Math.min(warmupSeconds, 900));
        settlingSeconds = Math.max(0, Math.min(settlingSeconds, 120));
        maxPreparationSeconds = Math.max(1, Math.min(maxPreparationSeconds, 3600));
        maxWarmupSeconds = Math.max(1, Math.min(maxWarmupSeconds, 3600));
        frameCapacity = Math.max(1_000, Math.min(frameCapacity, 10_000_000));
        requiredWorldName = requiredWorldName == null || requiredWorldName.isBlank()
                ? "MineBench Suite 1" : requiredWorldName.trim();
        if (scenes == null || scenes.isEmpty()) {
            scenes = new ArrayList<>(List.of("overworld_panorama"));
        } else {
            scenes = new ArrayList<>(scenes.stream()
                    .filter(id -> id != null && SceneDefinitions.find(id).isPresent())
                    .distinct()
                    .toList());
            if (scenes.isEmpty()) {
                scenes.add("overworld_panorama");
            }
        }
    }
}
