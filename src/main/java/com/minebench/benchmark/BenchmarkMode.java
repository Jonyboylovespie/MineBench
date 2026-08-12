package com.minebench.benchmark;

public enum BenchmarkMode {
    QUICK,
    STANDARD,
    EXTENDED,
    MANAGED;

    public static BenchmarkMode parse(String value) {
        if (value == null) {
            return STANDARD;
        }
        return switch (value.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "quick" -> QUICK;
            case "extended", "suite" -> EXTENDED;
            case "managed" -> MANAGED;
            default -> STANDARD;
        };
    }
}
