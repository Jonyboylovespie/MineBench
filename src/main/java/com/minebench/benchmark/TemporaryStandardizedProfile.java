package com.minebench.benchmark;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Declarative setting profile. The client adapter decides which Minecraft options are safely writable; no third-party
 * mod setting is touched here.
 */
public final class TemporaryStandardizedProfile {
    private final Map<String, String> desiredValues;

    public TemporaryStandardizedProfile(Map<String, String> desiredValues) {
        this.desiredValues = new LinkedHashMap<>(desiredValues == null ? Map.of() : desiredValues);
    }

    public static TemporaryStandardizedProfile suggested() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("enableVsync", "false");
        values.put("framerateLimit", "unlimited");
        values.put("renderDistance", "12");
        values.put("simulationDistance", "8");
        values.put("graphicsMode", "fancy");
        values.put("particles", "all");
        values.put("renderClouds", "false");
        values.put("biomeBlendRadius", "5");
        return new TemporaryStandardizedProfile(values);
    }

    public Map<String, String> desiredValues() {
        return Map.copyOf(desiredValues);
    }

    public SettingsSnapshot snapshot(SettingsAccessor accessor) {
        SettingsSnapshot snapshot = new SettingsSnapshot();
        snapshot.mode = "temporary-standardized-profile";
        for (String key : desiredValues.keySet()) {
            snapshot.values.put(key, accessor.read(key));
        }
        return snapshot;
    }

    public SettingsSnapshot apply(SettingsAccessor accessor) {
        SettingsSnapshot snapshot = snapshot(accessor);
        for (Map.Entry<String, String> entry : desiredValues.entrySet()) {
            String current = accessor.read(entry.getKey());
            if (!java.util.Objects.equals(current, entry.getValue())) {
                accessor.write(entry.getKey(), entry.getValue());
                snapshot.changedValues.put(entry.getKey(), entry.getValue());
            }
        }
        return snapshot;
    }

    public void restore(SettingsAccessor accessor, SettingsSnapshot snapshot) {
        if (snapshot == null || snapshot.values == null) return;
        for (Map.Entry<String, String> entry : snapshot.values.entrySet()) {
            accessor.write(entry.getKey(), entry.getValue());
        }
    }
}
