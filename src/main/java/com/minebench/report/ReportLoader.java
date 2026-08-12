package com.minebench.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class ReportLoader {
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues().create();

    private ReportLoader() {
    }

    public static BenchmarkReport read(Path reportJson) throws IOException {
        BenchmarkReport report = GSON.fromJson(Files.readString(reportJson, StandardCharsets.UTF_8), BenchmarkReport.class);
        if (report == null) throw new IOException("Report is empty: " + reportJson);
        return report;
    }

    public static List<Path> list(Path resultsRoot) throws IOException {
        if (!Files.isDirectory(resultsRoot)) return List.of();
        try (var paths = Files.list(resultsRoot)) {
            return paths.filter(Files::isDirectory)
                    .filter(path -> Files.isRegularFile(path.resolve("report.json")))
                    .filter(path -> Files.isRegularFile(path.resolve("complete.marker")))
                    .sorted(Comparator.comparing(Path::toString).reversed())
                    .toList();
        }
    }
}
