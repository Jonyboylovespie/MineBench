package com.minebench.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/** Small opt-in API. Integrations can describe settings without changing MineBench methodology. */
public final class MineBenchApi {
    private static final CopyOnWriteArrayList<BenchmarkMetadataProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private MineBenchApi() {
    }

    public static void registerMetadataProvider(BenchmarkMetadataProvider provider) {
        if (provider != null) {
            PROVIDERS.addIfAbsent(provider);
        }
    }

    /** Collects and sorts metadata outside the measured frame loop. Provider failures are isolated. */
    public static Map<String, String> collectMetadata() {
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        for (BenchmarkMetadataProvider provider : PROVIDERS) {
            try {
                Map<String, String> metadata = provider.metadata();
                if (metadata != null) {
                    entries.addAll(metadata.entrySet());
                }
            } catch (RuntimeException ignored) {
                // Optional metadata must never make a benchmark unusable.
            }
        }
        entries.sort(Comparator.comparing(Map.Entry::getKey));
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(result);
    }
}
