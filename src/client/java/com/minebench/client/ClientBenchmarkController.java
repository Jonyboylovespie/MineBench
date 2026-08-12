package com.minebench.client;

import com.minebench.benchmark.BenchmarkController;
import com.minebench.benchmark.BenchmarkPlan;
import com.minebench.benchmark.BenchmarkState;
import com.minebench.benchmark.ManagedRunPlan;
import com.minebench.benchmark.ValidityCode;
import com.minebench.report.AtomicReportStore;
import com.minebench.report.BenchmarkReport;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

/** Client adapter that keeps Minecraft concerns at the edge of the platform-neutral controller. */
public final class ClientBenchmarkController {
    private final BenchmarkController core;
    private final ClientCameraRoute cameraRoute = new ClientCameraRoute();
    private final ClientSceneCoordinator sceneCoordinator = new ClientSceneCoordinator();
    private BenchmarkReport latestReport;
    private long phaseStartedNanos;
    private long measurementDeadlineNanos;
    private boolean managed;
    private ManagedRunPlan managedPlan;
    private String measuredWindowSignature;
    private String measuredSettingsSignature;
    private String measuredWorldSignature;
    private long preparationDeadlineNanos;
    private final EnumSet<ValidityCode> reportedContamination = EnumSet.noneOf(ValidityCode.class);
    private final Map<String, String> routeHashes = new LinkedHashMap<>();

    public ClientBenchmarkController() {
        this(900_000);
    }

    public ClientBenchmarkController(int frameCapacity) {
        core = new BenchmarkController(frameCapacity);
    }

    public BenchmarkController core() {
        return core;
    }

    public BenchmarkReport latestReport() {
        return latestReport;
    }

    public boolean active() {
        return core.state() != BenchmarkState.IDLE
                && core.state() != BenchmarkState.COMPLETE;
    }

    public void startQuick(Minecraft client) {
        start(client, BenchmarkPlan.quick(), false, null);
    }

    public void startStandard(Minecraft client, String label) {
        start(client, BenchmarkPlan.standard(label), false, null);
    }

    public void startExtended(Minecraft client, String label) {
        start(client, BenchmarkPlan.extended(label), false, null);
    }

    public void startPlan(Minecraft client, BenchmarkPlan plan) {
        start(client, plan, false, null);
    }

    public void startManaged(Minecraft client, ManagedRunPlan plan) {
        start(client, plan.toBenchmarkPlan(), true, plan);
    }

    private void start(Minecraft client, BenchmarkPlan plan, boolean managedMode, ManagedRunPlan managedRunPlan) {
        if (active()) {
            client.gui.setScreen(new NoticeScreen(client.gui.screen(), "MineBench is already running", "Press Escape to abort."));
            return;
        }
        if (client.level == null || client.player == null) {
            client.gui.setScreen(new NoticeScreen(client.gui.screen(), "Load a world first",
                    "MineBench requires a local world before it can start."));
            return;
        }
        long now = System.nanoTime();
        managed = managedMode;
        managedPlan = managedRunPlan;
        routeHashes.clear();
        try {
            core.start(plan, now);
            RenderWorkloadController.begin(client, core.plan().workloadTargetValue());
            ClientSceneCoordinator.Validation validation = sceneCoordinator.validateWorld(client,
                    plan.expectedWorldSeed, plan.requiredWorldName, plan.requireDedicatedWorld);
            if (!validation.valid()) throw new IllegalStateException(validation.message());
            sceneCoordinator.captureOriginal(client);
            core.validationPassed(now);
            core.settingsSnapshotted(now);
            core.worldPrepared(now);
            beginScenePreparation(client, now);
            phaseStartedNanos = now;
            writeManagedStatusQuietly("running", null, "benchmark started");
            client.gui.setScreen(null);
        } catch (RuntimeException exception) {
            core.fail(exception.getMessage(), now);
            writeManagedStatusQuietly("failed", null, exception.getMessage());
            restoreAfterFailure(client, now);
            client.gui.setScreen(new NoticeScreen(client.gui.screen(), "MineBench could not start", exception.getMessage()));
        }
    }

    public void tick(Minecraft client, long now) {
        if (!active()) return;
        try {
            var throttleReason = client.getFramerateLimitTracker().getThrottleReason();
            if (core.state() == BenchmarkState.MEASURING
                    && throttleReason != com.mojang.blaze3d.platform.FramerateLimitTracker.FramerateThrottleReason.NONE) {
                invalidateOnce(ValidityCode.FRAMERATE_THROTTLED,
                        "Minecraft's framerate limiter was active during measurement: " + throttleReason, now);
            }
            // Scripted camera motion is not player input, so Minecraft's AFK limiter would otherwise cap a managed
            // benchmark to 30 FPS after one minute and 10 FPS after ten minutes. Keep the vanilla inactivity timer
            // fresh without changing or persisting the user's option.
            client.getFramerateLimitTracker().onInputReceived();
            cameraRoute.update(client, now);
            switch (core.state()) {
                case WAITING_FOR_CHUNKS -> {
                    if (sceneCoordinator.chunksReady(client)) {
                        core.chunksReady(now);
                    } else if (now >= preparationDeadlineNanos) {
                        fail(client, "Timed out waiting for required client chunks", now);
                    }
                }
                case WAITING_FOR_RENDERER -> {
                    if (sceneCoordinator.rendererReady(client)) {
                        startRouteIfNeeded(client, now);
                        core.rendererReady(now);
                        phaseStartedNanos = now;
                    } else if (now >= preparationDeadlineNanos) {
                        fail(client, "Timed out waiting for renderer section compilation", now);
                    }
                }
                case WARMING_UP -> {
                    long warmupNanos = core.plan().warmupSeconds * 1_000_000_000L;
                    long maxNanos = core.plan().maxWarmupSeconds * 1_000_000_000L;
                    long elapsed = now - phaseStartedNanos;
                    if (elapsed >= warmupNanos || elapsed >= maxNanos) {
                        core.finishWarmup(now, elapsed < maxNanos || warmupNanos == 0L);
                        phaseStartedNanos = now;
                    }
                }
                case SETTLING -> {
                    if (now - phaseStartedNanos >= core.plan().settlingSeconds * 1_000_000_000L) {
                        if (core.currentScene().geometryRebuild()) {
                            sceneCoordinator.invalidateGeometry(client);
                        }
                        cameraRoute.restart(now);
                        core.beginMeasurement(now);
                        measuredWindowSignature = ClientEnvironmentCollector.windowSignature(client);
                        measuredSettingsSignature = ClientEnvironmentCollector.settingsSignature(client);
                        measuredWorldSignature = ClientEnvironmentCollector.worldSignature(client);
                        reportedContamination.clear();
                        measurementDeadlineNanos = now + core.currentScene().durationSeconds() * 1_000_000_000L;
                    }
                }
                case MEASURING -> {
                    checkContamination(client, now);
                    if (now >= measurementDeadlineNanos) {
                        core.finishMeasurement(now);
                        if (core.advanceAfterProcessing(now)) {
                            phaseStartedNanos = now;
                            if (core.state() == BenchmarkState.SETTLING) {
                                sceneCoordinator.resetIterationWorkload();
                                cameraRoute.restart(now);
                            } else if (core.state() == BenchmarkState.LOADING_SCENE) {
                                beginScenePreparation(client, now);
                            }
                        } else {
                            writeReport(client, now);
                        }
                    }
                }
                default -> {
                }
            }
        } catch (RuntimeException exception) {
            fail(client, exception.getMessage(), now);
        }
    }

    private void checkContamination(Minecraft client, long now) {
        Boolean focused = ClientEnvironmentCollector.windowFocused(client);
        if (Boolean.FALSE.equals(focused)) {
            invalidateOnce(ValidityCode.FOCUS_LOST, "Window focus was lost during measurement", now);
        }
        if (client.gui.screen() != null) {
            invalidateOnce(ValidityCode.PAUSE_MENU_OPENED, "A screen was opened during measurement", now);
        }
        if (measuredWindowSignature != null
                && !measuredWindowSignature.equals(ClientEnvironmentCollector.windowSignature(client))) {
            invalidateOnce(ValidityCode.RESOLUTION_CHANGED, "Window or fullscreen state changed during measurement", now);
        }
        if (measuredSettingsSignature != null
                && !measuredSettingsSignature.equals(ClientEnvironmentCollector.settingsSignature(client))) {
            invalidateOnce(ValidityCode.SETTINGS_CHANGED, "A captured graphics setting changed during measurement", now);
        }
        if (measuredWorldSignature != null
                && !measuredWorldSignature.equals(ClientEnvironmentCollector.worldSignature(client))) {
            invalidateOnce(ValidityCode.DIMENSION_CHANGED, "World or dimension changed during measurement", now);
        }
    }

    private void invalidateOnce(ValidityCode code, String message, long now) {
        if (reportedContamination.add(code)) core.invalidate(code, message, now);
    }

    private void loadNextScene(long now) {
        beginScenePreparation(Minecraft.getInstance(), now);
    }

    private void beginScenePreparation(Minecraft client, long now) {
        cameraRoute.stop(client);
        core.sceneLoadingStarted(now);
        if (!sceneCoordinator.begin(client, core.currentScene(), now)) {
            throw new IllegalStateException("Could not prepare scene " + core.currentScene().id());
        }
        try {
            var route = ClientCameraRoute.load(client, core.currentScene());
            routeHashes.put(core.currentScene().id(), route == null ? "none" : route.routeHash());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not validate camera route for " + core.currentScene().id(), exception);
        }
        preparationDeadlineNanos = now + core.plan().maxPreparationSeconds * 1_000_000_000L;
        phaseStartedNanos = now;
        core.recordEvent("scene_prepare", core.currentScene().id() + " dimension="
                + core.currentScene().dimension() + " anchor=" + core.currentScene().anchorDescription(), now);
    }

    private void writeReport(Minecraft client, long now) {
        try {
            var scene = core.currentScene();
            var environment = ClientEnvironmentCollector.capture(client, scene == null ? "unavailable" : scene.id(),
                    scene == null ? 0 : scene.durationSeconds(), core.iterationIndex());
            environment.framebufferResolution = client.gameRenderer.mainRenderTarget().width + "x"
                    + client.gameRenderer.mainRenderTarget().height;
            environment.pathId = "multiple (see scenes)";
            environment.routeHash = "multiple (see scenes)";
            environment.sceneContentHash = "multiple (see scenes)";
            latestReport = core.buildReport(environment, Instant.now().toString());
            for (var sceneReport : latestReport.scenes) {
                var descriptor = com.minebench.scene.SceneDefinitions.find(sceneReport.sceneId).orElseThrow();
                sceneReport.routeHash = routeHashes.getOrDefault(sceneReport.sceneId, "unavailable");
                sceneReport.sceneContentHash = com.minebench.scene.SceneGenerator.blueprint(descriptor,
                        core.plan().expectedWorldSeed, sceneReport.routeHash).contentHash;
            }
            Path root = FabricLoader.getInstance().getGameDir().resolve("minebench-results");
            Path directory = AtomicReportStore.write(root, latestReport.label, latestReport.runId,
                    latestReport, core.frameSeries(), core.events());
            core.markShowingResults(now);
            writeManagedStatus("success", directory.toString(), "report written");
            RenderWorkloadController.end(client);
            client.gui.setScreen(new ResultsScreen(null, latestReport, directory));
            cameraRoute.stop(client);
            sceneCoordinator.restoreOriginal(client);
            if (managedPlan != null && managedPlan.exitOnCompletion) {
                try {
                    client.getClass().getMethod("stop").invoke(client);
                } catch (ReflectiveOperationException exception) {
                    System.err.println("[MineBench] Managed exit requested but Minecraft.stop was unavailable: "
                            + exception.getMessage());
                }
            }
        } catch (IOException | RuntimeException exception) {
            core.fail("Report write failed: " + exception.getMessage(), now);
            writeManagedStatusQuietly("failed", null, exception.getMessage());
            restoreAfterFailure(client, now);
            client.gui.setScreen(new NoticeScreen(null, "MineBench report failed", exception.getMessage()));
        }
    }

    public void abort(Minecraft client) {
        if (!active()) return;
        long now = System.nanoTime();
        if (core.state() == BenchmarkState.SHOWING_RESULTS) {
            core.complete(now);
            return;
        }
        core.abort("user pressed Escape", now);
        cameraRoute.stop(client);
        sceneCoordinator.restoreOriginal(client);
        RenderWorkloadController.end(client);
        writeManagedStatusQuietly("aborted", null, "user aborted benchmark");
        core.beginRestoration(now);
        core.complete(now);
        client.gui.setScreen(new NoticeScreen(null, "MineBench aborted", "No completed report was written."));
    }

    private void startRouteIfNeeded(Minecraft client, long now) {
        var scene = core.currentScene();
        if (scene == null || !scene.hasCameraRoute()) {
            if (!cameraRoute.lockAt(client, scene, now)) {
                core.invalidate(ValidityCode.CAMERA_DEVIATED, "The static-scene camera could not be locked", now);
            }
            return;
        }
        if (cameraRoute.active() && !"unavailable".equals(cameraRoute.pathId())) return;
        if (cameraRoute.active()) cameraRoute.stop(client);
        if (!cameraRoute.active() && !cameraRoute.start(client, scene, now)) {
            core.invalidate(ValidityCode.CAMERA_DEVIATED, "The scripted flythrough route could not be loaded", now);
        }
    }

    private void fail(Minecraft client, String message, long now) {
        core.fail(message == null || message.isBlank() ? "Unexpected benchmark failure" : message, now);
        writeManagedStatusQuietly("failed", null, message);
        restoreAfterFailure(client, now);
        client.gui.setScreen(new NoticeScreen(null, "MineBench stopped", message));
    }

    private void restoreAfterFailure(Minecraft client, long now) {
        cameraRoute.stop(client);
        sceneCoordinator.restoreOriginal(client);
        RenderWorkloadController.end(client);
        if (core.state() == BenchmarkState.FAILED) {
            core.beginRestoration(now);
            core.complete(now);
        }
    }

    private void writeManagedStatus(String status, String directory, String message) throws IOException {
        if (!managed || managedPlan == null) return;
        Path statusPath = FabricLoader.getInstance().getGameDir().resolve(managedPlan.statusFile);
        ManagedStatusStore.write(statusPath, status, core.runId(), directory, message);
    }

    private void writeManagedStatusQuietly(String status, String directory, String message) {
        try {
            writeManagedStatus(status, directory, message);
        } catch (IOException ignored) {
            System.err.println("[MineBench] Could not write managed status; report data was left untouched.");
        }
    }
}
