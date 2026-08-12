package com.minebench.benchmark;

import java.nio.file.Path;

/** Platform-neutral managed-mode policy; file/process operations stay in the client adapter or external script. */
public final class ManagedRunController {
    private final ManagedRunPlan plan;

    public ManagedRunController(ManagedRunPlan plan) {
        if (plan == null) throw new IllegalArgumentException("Managed plan is required");
        plan.sanitize();
        this.plan = plan;
    }

    public ManagedRunPlan plan() {
        return plan;
    }

    public BenchmarkPlan benchmarkPlan() {
        return plan.toBenchmarkPlan();
    }

    public boolean shouldAutoStart(boolean launchMarkerPresent) {
        return plan.autoStart || launchMarkerPresent;
    }

    public Path statusPath(Path gameDirectory) {
        return gameDirectory.resolve(plan.statusFile).normalize();
    }
}
