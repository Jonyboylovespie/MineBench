package com.minebench.environment;

import java.util.List;

public record EnvironmentComparison(boolean compatible, List<EnvironmentDifference> differences) {
    public EnvironmentComparison {
        differences = List.copyOf(differences == null ? List.of() : differences);
    }
}
