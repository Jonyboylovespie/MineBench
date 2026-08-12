package com.minebench.benchmark;

@FunctionalInterface
public interface SettingsAccessor {
    String read(String key);

    default void write(String key, String value) {
        throw new UnsupportedOperationException("This accessor is read-only");
    }
}
