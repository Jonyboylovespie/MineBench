package com.minebench.benchmark;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingsProfileTest {
    @Test
    void standardizedProfileRestoresSnapshotValues() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("enableVsync", "true");
        values.put("framerateLimit", "60");
        SettingsAccessor accessor = new SettingsAccessor() {
            @Override
            public String read(String key) {
                return values.get(key);
            }

            @Override
            public void write(String key, String value) {
                values.put(key, value);
            }
        };
        TemporaryStandardizedProfile profile = new TemporaryStandardizedProfile(
                Map.of("enableVsync", "false", "framerateLimit", "unlimited"));
        SettingsSnapshot snapshot = profile.apply(accessor);
        assertEquals("false", values.get("enableVsync"));
        profile.restore(accessor, snapshot);
        assertEquals("true", values.get("enableVsync"));
        assertEquals("60", values.get("framerateLimit"));
    }
}
