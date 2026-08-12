package com.minebench.scene;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SceneManifestStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private SceneManifestStore() {
    }

    public static SceneManifest read(Path file) throws IOException {
        SceneManifest manifest = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), SceneManifest.class);
        if (manifest == null) throw new IOException("Scene manifest is empty: " + file);
        return manifest;
    }

    public static void write(Path file, SceneManifest manifest) throws IOException {
        Path parent = file.toAbsolutePath().normalize().getParent();
        if (parent != null) Files.createDirectories(parent);
        Files.writeString(file, GSON.toJson(manifest), StandardCharsets.UTF_8);
    }
}
