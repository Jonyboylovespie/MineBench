package com.minebench.api;

import java.util.Map;

/** Optional, renderer-neutral hook for mods to expose human-readable benchmark configuration metadata. */
@FunctionalInterface
public interface BenchmarkMetadataProvider {
    Map<String, String> metadata();
}
