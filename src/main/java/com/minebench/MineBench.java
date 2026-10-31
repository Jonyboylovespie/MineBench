package com.minebench;

import net.fabricmc.api.ModInitializer;

/** Common entry point. The benchmark itself is client-only; the pure core remains usable by tooling and tests. */
public final class MineBench implements ModInitializer {
    public static final String MOD_ID = "minebench";
    public static final String MOD_VERSION = "1.0.1";
    public static final String SUITE_VERSION = "1";
    public static final int REPORT_SCHEMA_VERSION = 1;

    @Override
    public void onInitialize() {
        // Client behavior is registered from MineBenchClient.
    }
}
