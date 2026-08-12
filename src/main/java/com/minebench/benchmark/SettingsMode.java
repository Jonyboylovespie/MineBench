package com.minebench.benchmark;

public enum SettingsMode {
    RECORD_ONLY,
    TEMPORARY_STANDARDIZED_PROFILE;

    public static SettingsMode parse(String value) {
        if (value == null) {
            return RECORD_ONLY;
        }
        return switch (value.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "temporary", "temporary-standardized-profile", "standardized" -> TEMPORARY_STANDARDIZED_PROFILE;
            default -> RECORD_ONLY;
        };
    }
}
