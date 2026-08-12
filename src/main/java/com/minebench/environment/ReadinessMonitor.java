package com.minebench.environment;

import java.util.ArrayList;
import java.util.List;

/** Generic readiness fallback. Renderer adapters can add checks without changing the state machine. */
public final class ReadinessMonitor {
    private final List<String> blockers = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public void require(boolean condition, String message) {
        if (!condition) blockers.add(message);
    }

    public void warn(boolean condition, String message) {
        if (!condition) warnings.add(message);
    }

    public ReadinessResult result() {
        return new ReadinessResult(blockers.isEmpty(), blockers, warnings);
    }
}
