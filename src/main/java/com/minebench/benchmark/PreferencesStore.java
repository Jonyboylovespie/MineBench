package com.minebench.benchmark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

/** Corrupt preferences are preserved with a recovery suffix before defaults are returned. */
public final class PreferencesStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private PreferencesStore() {
    }

    public static BenchmarkPreferences load(Path file) throws IOException {
        if (!Files.isRegularFile(file)) return defaults();
        try {
            BenchmarkPreferences preferences = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8),
                    BenchmarkPreferences.class);
            if (preferences == null) throw new JsonParseException("empty preferences");
            preferences.sanitize();
            return preferences;
        } catch (JsonParseException | IllegalStateException exception) {
            Path backup = file.resolveSibling(file.getFileName() + ".corrupt-" + Instant.now().toEpochMilli());
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            return defaults();
        }
    }

    public static void save(Path file, BenchmarkPreferences preferences) throws IOException {
        if (preferences == null) preferences = defaults();
        preferences.sanitize();
        Path parent = file.toAbsolutePath().normalize().getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, GSON.toJson(preferences), StandardCharsets.UTF_8);
        try {
            Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static BenchmarkPreferences defaults() {
        BenchmarkPreferences preferences = new BenchmarkPreferences();
        preferences.sanitize();
        return preferences;
    }
}
