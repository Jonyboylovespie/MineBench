package com.minebench.benchmark;

public record BenchmarkTransition(
        BenchmarkState from,
        BenchmarkState to,
        String reason,
        long monotonicNanos
) {
}
