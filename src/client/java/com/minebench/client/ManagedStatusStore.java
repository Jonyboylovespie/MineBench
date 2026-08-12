package com.minebench.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

public final class ManagedStatusStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ManagedStatusStore() {
    }

    public static void write(Path file, String status, String runId, String reportDirectory, String message) throws IOException {
        Path parent = file.toAbsolutePath().normalize().getParent();
        if (parent != null) Files.createDirectories(parent);
        Status value = new Status(1, status, runId, reportDirectory, message, Instant.now().toString());
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, GSON.toJson(value), StandardCharsets.UTF_8);
        try {
            Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record Status(int schemaVersion, String status, String runId, String reportDirectory, String message,
                          String timestamp) {
    }
}
