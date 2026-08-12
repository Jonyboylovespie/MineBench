package com.minebench.environment;

import java.util.List;

public record ReadinessResult(boolean ready, List<String> blockers, List<String> warnings) {
    public ReadinessResult {
        blockers = List.copyOf(blockers == null ? List.of() : blockers);
        warnings = List.copyOf(warnings == null ? List.of() : warnings);
    }
}
