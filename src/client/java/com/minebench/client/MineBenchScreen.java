package com.minebench.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Compact entry screen; detailed reports are deliberately generated after measurement, not rebuilt per frame. */
public final class MineBenchScreen extends Screen {
    private final Screen parent;

    public MineBenchScreen(Screen parent) {
        super(Component.literal("MineBench"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = width / 2 - 155;
        int right = width / 2 + 5;
        int y = 42;
        addRenderableWidget(CycleButton.<com.minebench.benchmark.WorkloadTarget>builder(
                        target -> Component.literal(target.displayName()), MineBenchClient.workloadTarget())
                .withValues(java.util.List.of(
                        com.minebench.benchmark.WorkloadTarget.CPU,
                        com.minebench.benchmark.WorkloadTarget.BALANCED,
                        com.minebench.benchmark.WorkloadTarget.GPU))
                .create(left, y, 310, 20, Component.literal("Workload target"),
                        (button, target) -> MineBenchClient.setWorkloadTarget(target)));
        y += 25;
        addRenderableWidget(Button.builder(Component.literal(BenchmarkWorldManager.exists(Minecraft.getInstance())
                        ? "Open Suite 1 world" : "Create Suite 1 world"), button -> {
            BenchmarkWorldManager.createOrOpen(Minecraft.getInstance(),
                    MineBenchClient.applyPreferences(com.minebench.benchmark.BenchmarkPlan.standard("Baseline")), this);
        }).bounds(left, y, 310, 20).build());
        y += 25;
        addRenderableWidget(Button.builder(Component.literal("Quick benchmark"), button -> {
            openPreflight(MineBenchClient.applyPreferences(com.minebench.benchmark.BenchmarkPlan.quick()));
        }).bounds(left, y, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Standard comparison"), button -> {
            openPreflight(MineBenchClient.applyPreferences(com.minebench.benchmark.BenchmarkPlan.standard("Baseline")));
        }).bounds(right, y, 150, 20).build());
        y += 25;
        addRenderableWidget(Button.builder(Component.literal("Extended 10-run suite"), button -> {
            openPreflight(MineBenchClient.applyPreferences(com.minebench.benchmark.BenchmarkPlan.extended("Extended suite")));
        }).bounds(left, y, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Managed mode"), button -> {
            Minecraft.getInstance().gui.setScreen(new NoticeScreen(this, "Managed mode",
                    "Place config/minebench/managed-plan.json in the instance and launch with -Dminebench.managed=true."));
        }).bounds(right, y, 150, 20).build());
        y += 25;
        addRenderableWidget(Button.builder(Component.literal("Methodology"), button -> {
            Minecraft.getInstance().gui.setScreen(new NoticeScreen(this, "Methodology",
                    "Frame intervals are primary. Results retain every iteration and raw frames; see METHODOLOGY.md."));
        }).bounds(left, y, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(right, y, 150, 20).build());
    }

    private void openPreflight(com.minebench.benchmark.BenchmarkPlan plan) {
        Minecraft.getInstance().gui.setScreen(new PreflightScreen(this, plan));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xD0101010);
        graphics.centeredText(font, title, width / 2, 14, 0xFFFFFFFF);
        graphics.centeredText(font, Component.literal("Deterministic rendering benchmarks • Suite 1 • client-only"),
                width / 2, 27, 0xFFB0B0B0);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().gui.setScreen(parent);
    }
}
