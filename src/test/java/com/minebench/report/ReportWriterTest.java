package com.minebench.report;

import com.minebench.capture.PrimitiveSampleBuffer;
import com.minebench.environment.EnvironmentFingerprint;
import com.minebench.stats.FrameStatistics;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportWriterTest {
    @Test
    void atomicStoreWritesAllMandatoryArtifacts() throws Exception {
        BenchmarkReport report = new BenchmarkReport();
        report.label = "Test report";
        report.environment = EnvironmentFingerprint.unavailable();
        SceneReport scene = new SceneReport();
        scene.sceneId = "overworld_panorama";
        scene.displayName = "Overworld static panorama";
        scene.durationSeconds = 30;
        scene.iterations = java.util.List.of();
        scene.aggregate = new com.minebench.stats.RunAggregate("overworld_panorama", 0, 0, 0, 0,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                com.minebench.stats.ConfidenceInterval.unavailable());
        report.scenes.add(scene);
        PrimitiveSampleBuffer buffer = new PrimitiveSampleBuffer(4, false);
        buffer.acceptTimestamp(0, 0);
        buffer.acceptTimestamp(16_000_000, 0);
        var root = Files.createTempDirectory("minebench-report-test");
        var directory = AtomicReportStore.write(root, report.label, "test-run", report,
                java.util.List.of(new FrameSeries("overworld_panorama", 1, buffer.snapshot())), java.util.List.of());
        assertTrue(Files.isRegularFile(directory.resolve("summary.md")));
        assertTrue(Files.isRegularFile(directory.resolve("report.json")));
        assertTrue(Files.isRegularFile(directory.resolve("frames.csv")));
        assertTrue(Files.isRegularFile(directory.resolve("environment.json")));
        assertTrue(Files.isRegularFile(directory.resolve("events.json")));
        assertTrue(Files.isRegularFile(directory.resolve("complete.marker")));
    }
}
