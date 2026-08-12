package com.minebench.benchmark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Atomic recovery file for temporary settings. It is written before any setting is changed. */
public final class RecoveryStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private RecoveryStore() {
    }

    public static void write(Path file, SettingsSnapshot snapshot) throws IOException {
        Path parent = file.toAbsolutePath().normalize().getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, GSON.toJson(snapshot), StandardCharsets.UTF_8);
        moveAtomically(temp, file);
    }

    public static SettingsSnapshot read(Path file) throws IOException {
        return GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), SettingsSnapshot.class);
    }

    public static void clear(Path file) throws IOException {
        Files.deleteIfExists(file);
    }

    private static void moveAtomically(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
