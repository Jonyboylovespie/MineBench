package com.minebench.benchmark;

import java.util.Locale;

/** Selects which side of the render pipeline MineBench should deliberately pressure. */
public enum WorkloadTarget {
    CPU("cpu", "CPU bottleneck", 0.5),
    BALANCED("balanced", "Balanced", 1.0),
    GPU("gpu", "GPU bottleneck", 2.0);

    private final String id;
    private final String displayName;
    private final double renderScale;

    WorkloadTarget(String id, String displayName, double renderScale) {
        this.id = id;
        this.displayName = displayName;
        this.renderScale = renderScale;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public double renderScale() {
        return renderScale;
    }

    public static WorkloadTarget parse(String value) {
        if (value == null) return BALANCED;
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "cpu", "cpu-bound", "cpu_bottleneck" -> CPU;
            case "gpu", "gpu-bound", "gpu_bottleneck" -> GPU;
            default -> BALANCED;
        };
    }
}
