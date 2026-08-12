package com.minebench.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JsonReportWriter {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeSpecialFloatingPointValues()
            .create();

    private JsonReportWriter() {
    }

    public static String toJson(BenchmarkReport report) {
        return GSON.toJson(report);
    }

    public static void write(Path file, BenchmarkReport report) throws java.io.IOException {
        Files.writeString(file, toJson(report), StandardCharsets.UTF_8);
    }
}
