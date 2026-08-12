package com.minebench.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ComparisonReportWriter {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .serializeSpecialFloatingPointValues().create();

    private ComparisonReportWriter() {
    }

    public static void write(Path file, ComparisonReport report) throws IOException {
        Files.writeString(file, GSON.toJson(report), StandardCharsets.UTF_8);
    }
}
