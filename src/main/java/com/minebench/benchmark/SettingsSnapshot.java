package com.minebench.benchmark;

import java.util.LinkedHashMap;
import java.util.Map;

/** Sanitized, renderer-neutral snapshot used for recovery and comparison reporting. */
public final class SettingsSnapshot {
    public int schemaVersion = 1;
    public String capturedAt;
    public String mode = "record-only";
    public Map<String, String> values = new LinkedHashMap<>();
    public Map<String, String> changedValues = new LinkedHashMap<>();

    public SettingsSnapshot copy() {
        SettingsSnapshot copy = new SettingsSnapshot();
        copy.schemaVersion = schemaVersion;
        copy.capturedAt = capturedAt;
        copy.mode = mode;
        copy.values = new LinkedHashMap<>(values == null ? Map.of() : values);
        copy.changedValues = new LinkedHashMap<>(changedValues == null ? Map.of() : changedValues);
        return copy;
    }

    public Map<String, String> differences(SettingsSnapshot other) {
        Map<String, String> differences = new LinkedHashMap<>();
        if (other == null) {
            differences.put("snapshot", "missing");
            return differences;
        }
        Map<String, String> left = values == null ? Map.of() : values;
        Map<String, String> right = other.values == null ? Map.of() : other.values;
        java.util.Set<String> keys = new java.util.TreeSet<>();
        keys.addAll(left.keySet());
        keys.addAll(right.keySet());
        for (String key : keys) {
            String a = left.get(key);
            String b = right.get(key);
            if (!java.util.Objects.equals(a, b)) {
                differences.put(key, String.valueOf(a) + " -> " + String.valueOf(b));
            }
        }
        return differences;
    }
}
