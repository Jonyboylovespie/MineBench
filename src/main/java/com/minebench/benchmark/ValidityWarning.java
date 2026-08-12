package com.minebench.benchmark;

public record ValidityWarning(
        ValidityCode code,
        ValiditySeverity severity,
        String message,
        long monotonicNanos,
        boolean invalidatesIteration
) {
    public ValidityWarning {
        if (code == null || severity == null) {
            throw new IllegalArgumentException("Validity warnings need a code and severity");
        }
        message = message == null ? "" : message;
    }
}
