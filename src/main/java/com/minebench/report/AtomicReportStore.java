package com.minebench.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minebench.capture.RuntimeEventRecorder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Writes every report artifact only after measurement and uses per-file atomic replacement. */
public final class AtomicReportStore {
    private static final DateTimeFormatter DIRECTORY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();

    private AtomicReportStore() {
    }

    public static Path write(
            Path resultsRoot,
            String label,
            String runId,
            BenchmarkReport report,
            List<FrameSeries> frames,
            List<RuntimeEventRecorder.RuntimeEvent> runtimeEvents
    ) throws IOException {
        Files.createDirectories(resultsRoot);
        String safeLabel = slug(label);
        String safeRunId = slug(runId);
        Path directory = resultsRoot.resolve(LocalDateTime.now().format(DIRECTORY_TIME) + "_" + safeLabel + "_" + safeRunId);
        Files.createDirectories(directory);

        atomicWrite(directory.resolve("summary.md"), MarkdownReportWriter.toMarkdown(report));
        atomicWrite(directory.resolve("report.json"), JsonReportWriter.toJson(report));
        atomicWrite(directory.resolve("environment.json"), GSON.toJson(report.environment.sanitizedCopy()));
        atomicWrite(directory.resolve("events.json"), GSON.toJson(new EventsDocument(report.phaseTransitions, runtimeEvents, report.warnings)));

        Path framesTemp = directory.resolve("frames.csv.tmp");
        CsvFrameWriter.write(framesTemp, frames);
        moveAtomically(framesTemp, directory.resolve("frames.csv"));
        Path chartTemp = directory.resolve("frame-times.svg.tmp");
        ChartWriter.writeFrameTimeChart(chartTemp, frames);
        moveAtomically(chartTemp, directory.resolve("frame-times.svg"));
        atomicWrite(directory.resolve("complete.marker"), "MineBench report complete\n");
        return directory;
    }

    private static void atomicWrite(Path file, String content) throws IOException {
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, content, StandardCharsets.UTF_8);
        moveAtomically(temp, file);
    }

    private static void moveAtomically(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String slug(String value) {
        if (value == null || value.isBlank()) return "unnamed";
        String result = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]+", "-");
        return result.replaceAll("^-+|-+$", "");
    }

    private record EventsDocument(
            List<com.minebench.benchmark.BenchmarkTransition> phaseTransitions,
            List<RuntimeEventRecorder.RuntimeEvent> runtimeEvents,
            List<com.minebench.benchmark.ValidityWarning> warnings
    ) {
    }
}
