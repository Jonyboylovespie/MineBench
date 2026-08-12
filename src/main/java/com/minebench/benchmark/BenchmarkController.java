package com.minebench.benchmark;

import com.minebench.capture.FrameSampler;
import com.minebench.capture.MemorySampler;
import com.minebench.capture.RuntimeEventRecorder;
import com.minebench.environment.EnvironmentFingerprint;
import com.minebench.report.BenchmarkReport;
import com.minebench.report.IterationReport;
import com.minebench.report.SceneReport;
import com.minebench.scene.SceneDescriptor;
import com.minebench.stats.IterationResult;
import com.minebench.stats.BottleneckMetrics;
import com.minebench.stats.RunAggregate;
import com.minebench.stats.RunAggregator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Platform-neutral session coordinator. Minecraft callbacks only advance this object; phase logic is not scattered
 * across unrelated callbacks.
 */
public final class BenchmarkController {
    private final BenchmarkStateMachine stateMachine = new BenchmarkStateMachine();
    private final FrameSampler frameSampler;
    private final RuntimeEventRecorder events = new RuntimeEventRecorder();
    private final List<IterationResult> results = new ArrayList<>();
    private final List<com.minebench.report.FrameSeries> frameSeries = new ArrayList<>();

    private BenchmarkPlan plan;
    private String runId;
    private String startedAt;
    private int sceneIndex;
    private int iterationIndex;
    private BenchmarkValidity currentValidity = new BenchmarkValidity();
    private MemorySampler.RuntimeCounters runtimeBefore;

    public BenchmarkController(int frameCapacity) {
        frameSampler = new FrameSampler(frameCapacity, true);
    }

    public BenchmarkState state() {
        return stateMachine.state();
    }

    public BenchmarkStateMachine stateMachine() {
        return stateMachine;
    }

    public BenchmarkPlan plan() {
        return plan;
    }

    public String runId() {
        return runId;
    }

    public SceneDescriptor currentScene() {
        if (plan == null || sceneIndex >= plan.sceneDescriptors().size()) {
            return null;
        }
        return plan.sceneDescriptors().get(sceneIndex);
    }

    public int sceneIndex() {
        return sceneIndex;
    }

    public int iterationIndex() {
        return iterationIndex;
    }

    public List<IterationResult> results() {
        return List.copyOf(results);
    }

    public List<com.minebench.report.FrameSeries> frameSeries() {
        return List.copyOf(frameSeries);
    }

    public List<RuntimeEventRecorder.RuntimeEvent> events() {
        return events.events();
    }

    public void start(BenchmarkPlan nextPlan, long now) {
        if (state() == BenchmarkState.COMPLETE) {
            stateMachine.transition(BenchmarkState.IDLE, "new benchmark requested", now);
            stateMachine.reset();
        }
        if (state() != BenchmarkState.IDLE) {
            throw new IllegalStateException("A benchmark is already active");
        }
        plan = nextPlan == null ? BenchmarkPlan.standard("Unnamed") : nextPlan;
        plan.sanitize();
        runId = UUID.randomUUID().toString();
        startedAt = Instant.now().toString();
        sceneIndex = 0;
        iterationIndex = 0;
        results.clear();
        frameSeries.clear();
        events.clear();
        currentValidity = new BenchmarkValidity();
        stateMachine.transition(BenchmarkState.VALIDATING_ENVIRONMENT, "benchmark requested", now);
        events.record("benchmark_start", plan.label, now);
    }

    public void validationPassed(long now) {
        transition(BenchmarkState.SNAPSHOTTING_SETTINGS, "environment preflight passed", now);
    }

    public void settingsSnapshotted(long now) {
        transition(BenchmarkState.PREPARING_WORLD, "settings snapshot recorded", now);
    }

    public void worldPrepared(long now) {
        transition(BenchmarkState.LOADING_SCENE, "benchmark arena preparation complete", now);
    }

    public void sceneLoadingStarted(long now) {
        transition(BenchmarkState.WAITING_FOR_CHUNKS, "scene load requested", now);
    }

    public void chunksReady(long now) {
        transition(BenchmarkState.WAITING_FOR_RENDERER, "expected scene chunks are present", now);
    }

    public void rendererReady(long now) {
        transition(BenchmarkState.WARMING_UP, "renderer readiness checks passed", now);
    }

    public void finishWarmup(long now, boolean complete) {
        if (!complete) {
            invalidate(ValidityCode.WARMUP_INCOMPLETE, "Warm-up timeout reached before stability criteria passed", now);
        }
        transition(BenchmarkState.SETTLING, complete ? "warm-up complete" : "warm-up timeout acknowledged", now);
    }

    public void beginMeasurement(long now) {
        if (state() != BenchmarkState.SETTLING) {
            throw new IllegalStateException("Measurement can only begin after settling");
        }
        // A warm-up timeout is attached to the first iteration. Later iterations start with a clean validity record.
        if (!results.isEmpty()) {
            currentValidity = new BenchmarkValidity();
        }
        if (iterationIndex == 0) {
            iterationIndex = 1;
        }
        runtimeBefore = MemorySampler.read();
        frameSampler.beginMeasurement();
        transition(BenchmarkState.MEASURING, "measurement interval started", now);
    }

    /** This is the only per-presented-frame call in the normal CPU sampler. */
    public void onPresentedFrame(long now, int eventFlags) {
        onPresentedFrame(now, eventFlags, Double.NaN);
    }

    public void onPresentedFrame(long now, int eventFlags, double gpuUtilizationPercent) {
        if (state() == BenchmarkState.MEASURING) {
            frameSampler.onPresentedFrame(now, eventFlags, gpuUtilizationPercent);
        }
    }

    public void recordEvent(String type, String message, long now) {
        events.record(type, message, now);
    }

    public void warn(ValidityCode code, String message, long now) {
        currentValidity.warn(code, message, now);
        events.record("warning", code + ": " + message, now);
    }

    public void invalidate(ValidityCode code, String message, long now) {
        currentValidity.invalidate(code, message, now);
        events.record("invalidation", code + ": " + message, now);
    }

    public IterationResult finishMeasurement(long now) {
        if (state() != BenchmarkState.MEASURING) {
            throw new IllegalStateException("No measurement is active");
        }
        FrameSampler.SampledIteration sampled = frameSampler.endMeasurement();
        var snapshot = sampled.samples();
        if (snapshot.overflowed()) {
            invalidate(ValidityCode.SAMPLE_BUFFER_OVERFLOW,
                    "Raw frame sample capacity was exceeded; percentile statistics are incomplete", now);
        }
        MemorySampler.RuntimeCounters runtimeAfter = MemorySampler.read();
        var runtimeDelta = runtimeAfter.delta(runtimeBefore);
        var statistics = sampled.statistics().withRuntimeCounters(
                runtimeAfter.heapBytes(), runtimeDelta.gcCount(), runtimeDelta.gcPauseMillis());
        if (statistics.storedFrameCount == 0) {
            invalidate(ValidityCode.MISSING_RAW_SAMPLES, "No frame intervals were accepted", now);
        }
        BottleneckMetrics bottleneck = BottleneckMetrics.from(
                snapshot.gpuUtilizationPercent(), plan.workloadTargetValue());
        if (bottleneck.gpuTimingSamples == 0) {
            warn(ValidityCode.GPU_TIMING_UNAVAILABLE,
                    "No asynchronous GPU timing samples were available; bottleneck target could not be verified", now);
        } else if (!bottleneck.targetConfirmed) {
            warn(ValidityCode.WORKLOAD_TARGET_NOT_REACHED,
                    "Requested " + plan.workloadTargetValue().displayName() + " but measured "
                            + bottleneck.classification + " (median GPU utilization "
                            + String.format(java.util.Locale.ROOT, "%.1f%%", bottleneck.medianGpuUtilizationPercent) + ")", now);
        }
        boolean valid = currentValidity.isValid();
        IterationResult result = new IterationResult(
                currentScene().id(), iterationIndex, valid, currentValidity.warnings(), statistics, bottleneck, snapshot);
        results.add(result);
        frameSeries.add(new com.minebench.report.FrameSeries(currentScene().id(), iterationIndex, snapshot));
        transition(BenchmarkState.COOLING_DOWN, "measurement interval ended", now);
        transition(BenchmarkState.PROCESSING_RESULTS, "iteration statistics calculated", now);
        events.record("iteration_complete", currentScene().id() + " #" + iterationIndex, now);
        return result;
    }

    /** Advances to the next iteration or to report writing after PROCESSING_RESULTS. */
    public boolean advanceAfterProcessing(long now) {
        if (state() != BenchmarkState.PROCESSING_RESULTS) {
            throw new IllegalStateException("Results are not ready to advance");
        }
        if (iterationIndex < plan.iterations) {
            iterationIndex++;
            transition(BenchmarkState.SETTLING, "next iteration settling", now);
            return true;
        }
        if (sceneIndex + 1 < plan.sceneDescriptors().size()) {
            sceneIndex++;
            iterationIndex = 0;
            transition(BenchmarkState.LOADING_SCENE, "next scene requested", now);
            return true;
        }
        transition(BenchmarkState.WRITING_REPORTS, "all requested iterations completed", now);
        return false;
    }

    public void markShowingResults(long now) {
        transition(BenchmarkState.SHOWING_RESULTS, "report files written", now);
    }

    public void beginRestoration(long now) {
        if (state() == BenchmarkState.SHOWING_RESULTS || state() == BenchmarkState.ABORTED
                || state() == BenchmarkState.FAILED || state() == BenchmarkState.WRITING_REPORTS) {
            transition(BenchmarkState.RESTORING_SETTINGS, "restoring captured client state", now);
        }
    }

    public void complete(long now) {
        if (state() == BenchmarkState.SHOWING_RESULTS) {
            beginRestoration(now);
        }
        if (state() == BenchmarkState.RESTORING_SETTINGS || state() == BenchmarkState.ABORTED
                || state() == BenchmarkState.FAILED) {
            transition(BenchmarkState.COMPLETE, "settings and camera restored", now);
        }
    }

    public void abort(String reason, long now) {
        if (state() == BenchmarkState.IDLE || state() == BenchmarkState.COMPLETE) {
            return;
        }
        if (state() != BenchmarkState.ABORTED && state() != BenchmarkState.FAILED) {
            transition(BenchmarkState.ABORTED, reason == null ? "user aborted benchmark" : reason, now);
        }
        events.record("abort", reason, now);
    }

    public void fail(String reason, long now) {
        if (state() == BenchmarkState.IDLE || state() == BenchmarkState.COMPLETE) {
            return;
        }
        if (state() != BenchmarkState.FAILED) {
            transition(BenchmarkState.FAILED, reason == null ? "benchmark failed" : reason, now);
        }
        events.record("failure", reason, now);
    }

    public BenchmarkReport buildReport(EnvironmentFingerprint environment, String completedAt) {
        BenchmarkReport report = new BenchmarkReport();
        report.minebenchVersion = com.minebench.MineBench.MOD_VERSION;
        report.suiteVersion = plan.suiteVersion;
        report.runId = runId;
        report.label = plan.label;
        report.mode = plan.mode;
        report.settingsMode = plan.settingsMode;
        report.workloadTarget = plan.workloadTargetValue().id();
        report.renderScale = plan.workloadTargetValue().renderScale();
        report.startedAt = startedAt;
        report.completedAt = completedAt == null ? Instant.now().toString() : completedAt;
        report.environment = environment == null ? EnvironmentFingerprint.unavailable() : environment.sanitizedCopy();
        report.phaseTransitions = stateMachine.transitions();
        report.events = events.events();
        report.singleRunWarning = plan.iterations == 1;

        for (SceneDescriptor scene : plan.sceneDescriptors()) {
            RunAggregator aggregator = new RunAggregator(scene.id());
            SceneReport sceneReport = new SceneReport();
            sceneReport.sceneId = scene.id();
            sceneReport.displayName = scene.displayName();
            sceneReport.durationSeconds = scene.durationSeconds();
            sceneReport.workloadDescription = scene.workloadDescription();
            sceneReport.dimension = scene.dimension();
            sceneReport.anchor = scene.anchorDescription();
            sceneReport.readinessRadiusChunks = scene.readinessRadiusChunks();
            sceneReport.cameraResource = scene.cameraResource();
            sceneReport.geometryRebuild = scene.geometryRebuild();
            sceneReport.iterations = new ArrayList<>();
            for (IterationResult result : results) {
                if (scene.id().equals(result.sceneId())) {
                    aggregator.add(result);
                    sceneReport.iterations.add(IterationReport.from(result));
                    report.warnings.addAll(result.warnings());
                }
            }
            RunAggregate aggregate = aggregator.aggregate();
            sceneReport.aggregate = aggregate;
            sceneReport.bottleneck = BottleneckMetrics.aggregate(sceneReport.iterations.stream()
                    .map(iteration -> iteration.bottleneck).toList(), plan.workloadTargetValue());
            report.scenes.add(sceneReport);
        }
        return report;
    }

    private void transition(BenchmarkState next, String reason, long now) {
        stateMachine.transition(next, reason, now);
    }
}
