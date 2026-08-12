package com.minebench.client;

import com.minebench.report.BenchmarkReport;
import com.minebench.report.SceneReport;
import com.minebench.benchmark.BenchmarkState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public final class ResultsScreen extends Screen {
    private final Screen parent;
    private final BenchmarkReport report;
    private final Path directory;

    public ResultsScreen(Screen parent, BenchmarkReport report, Path directory) {
        super(Component.literal("MineBench results"));
        this.parent = parent;
        this.report = report;
        this.directory = directory;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(width / 2 - 60, height - 30, 120, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xD0101010);
        graphics.centeredText(font, title, width / 2, 12, 0xFFFFFFFF);
        graphics.centeredText(font, Component.literal("Report: " + directory), width / 2, 25, 0xFFB0B0B0);
        int y = 48;
        for (SceneReport scene : report.scenes) {
            String line = scene.sceneId + "  median FPS: " + format(scene.aggregate.medianAverageFps)
                    + "  bottleneck: " + (scene.bottleneck == null ? "unavailable" : scene.bottleneck.classification)
                    + " (GPU " + (scene.bottleneck == null ? "unavailable" : format(scene.bottleneck.medianGpuUtilizationPercent)) + "%)"
                    + "  1% low: " + format(scene.aggregate.medianOnePercentLowFps)
                    + "  p99: " + format(scene.aggregate.medianP99FrameTimeMillis) + " ms"
                    + "  valid: " + scene.aggregate.validIterations + "/" + scene.aggregate.totalIterations;
            graphics.centeredText(font, Component.literal(line), width / 2, y, 0xFFE0E0E0);
            y += 14;
        }
        if (report.singleRunWarning) {
            graphics.centeredText(font, Component.literal("Warning: this is a single run; no confidence estimate is available."),
                    width / 2, y + 8, 0xFFFFC040);
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private static String format(double value) {
        return Double.isFinite(value) ? String.format(java.util.Locale.ROOT, "%.2f", value) : "unavailable";
    }

    @Override
    public void onClose() {
        if (MineBenchClient.controller() != null
                && MineBenchClient.controller().core().state() == BenchmarkState.SHOWING_RESULTS) {
            MineBenchClient.controller().core().complete(System.nanoTime());
        }
        Minecraft.getInstance().gui.setScreen(parent);
    }
}
