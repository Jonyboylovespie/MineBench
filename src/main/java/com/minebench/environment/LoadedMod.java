package com.minebench.environment;

public record LoadedMod(String id, String version) {
    public LoadedMod {
        id = id == null ? "unavailable" : id;
        version = version == null ? "unavailable" : version;
    }
}
