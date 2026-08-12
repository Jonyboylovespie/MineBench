package com.minebench.benchmark;

import com.minebench.environment.EnvironmentFingerprint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BenchmarkControllerTest {
    @Test
    void samplesOnlyDuringMeasurementAndBuildsPerSceneReport() {
        BenchmarkPlan plan = BenchmarkPlan.quick();
        plan.scenes = new java.util.ArrayList<>(java.util.List.of("overworld_panorama"));
        BenchmarkController controller = new BenchmarkController(32);
        controller.start(plan, 0L);
        controller.validationPassed(1L);
        controller.settingsSnapshotted(2L);
        controller.worldPrepared(3L);
        controller.sceneLoadingStarted(4L);
        controller.chunksReady(5L);
        controller.rendererReady(6L);
        controller.onPresentedFrame(7L, 0);
        controller.finishWarmup(8L, true);
        controller.beginMeasurement(10L);
        controller.onPresentedFrame(10L, 0);
        controller.onPresentedFrame(16_010_000L, 0);
        controller.onPresentedFrame(32_010_000L, 0);
        var result = controller.finishMeasurement(32_010_000L);
        assertEquals(2, result.statistics().storedFrameCount);
        assertTrue(result.valid());
        assertTrue(!controller.advanceAfterProcessing(33_000_000L));
        var report = controller.buildReport(EnvironmentFingerprint.unavailable(), "done");
        assertEquals(1, report.scenes.size());
        assertEquals(1, report.scenes.getFirst().aggregate.validIterations);
    }

    @Test
    void excludesFramerateThrottledIterationsFromAggregates() {
        BenchmarkPlan plan = BenchmarkPlan.quick();
        plan.scenes = new java.util.ArrayList<>(java.util.List.of("overworld_panorama"));
        BenchmarkController controller = new BenchmarkController(32);
        controller.start(plan, 0L);
        controller.validationPassed(1L);
        controller.settingsSnapshotted(2L);
        controller.worldPrepared(3L);
        controller.sceneLoadingStarted(4L);
        controller.chunksReady(5L);
        controller.rendererReady(6L);
        controller.finishWarmup(8L, true);
        controller.beginMeasurement(10L);
        controller.onPresentedFrame(10L, 0);
        controller.onPresentedFrame(33_343_333L, 0);
        controller.invalidate(ValidityCode.FRAMERATE_THROTTLED,
                "Minecraft's framerate limiter was active during measurement: SHORT_AFK", 33_343_333L);

        var result = controller.finishMeasurement(33_343_333L);

        assertFalse(result.valid());
        assertTrue(result.warnings().stream().anyMatch(warning -> warning.code() == ValidityCode.FRAMERATE_THROTTLED));
        assertFalse(controller.advanceAfterProcessing(34_000_000L));
        var report = controller.buildReport(EnvironmentFingerprint.unavailable(), "done");
        assertEquals(0, report.scenes.getFirst().aggregate.validIterations);
        assertEquals(1, report.scenes.getFirst().aggregate.invalidIterations);
    }
}
