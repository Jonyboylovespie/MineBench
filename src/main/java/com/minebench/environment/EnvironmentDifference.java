package com.minebench.environment;

public record EnvironmentDifference(String field, String left, String right, boolean blocksComparison) {
}
