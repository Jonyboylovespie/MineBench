package com.minebench.client;

import com.minebench.benchmark.BenchmarkPlan;
import com.minebench.environment.EnvironmentFingerprint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PreflightScreen extends Screen {
    private final Screen parent;
    private final BenchmarkPlan plan;
    private final EnvironmentFingerprint environment;
    private final ClientSceneCoordinator.Validation validation;

    public PreflightScreen(Screen parent, BenchmarkPlan plan) {
        super(Component.literal("MineBench preflight"));
        this.parent = parent;
        this.plan = plan;
        Minecraft client = Minecraft.getInstance();
        this.environment = ClientEnvironmentCollector.capture(client,
                plan.sceneDescriptors().isEmpty() ? "unavailable" : plan.sceneDescriptors().getFirst().id(),
                plan.sceneDescriptors().isEmpty() ? 0 : plan.sceneDescriptors().getFirst().durationSeconds(),
                1);
        this.validation = new ClientSceneCoordinator().validateWorld(client,
                plan.expectedWorldSeed, plan.requiredWorldName, plan.requireDedicatedWorld);
    }

    @Override
    protected void init() {
        Button start = Button.builder(Component.literal("Start benchmark"), button -> {
            MineBenchClient.controller().startPlan(Minecraft.getInstance(), plan);
        }).bounds(width / 2 - 155, height - 54, 150, 20).build();
        start.active = validation.valid();
        addRenderableWidget(start);
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
                .bounds(width / 2 + 5, height - 54, 150, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xD0101010);
        graphics.centeredText(font, title, width / 2, 12, 0xFFFFFFFF);
        graphics.centeredText(font, Component.literal("Mode: " + plan.mode + " • label: " + plan.label
                + " • iterations: " + plan.iterations), width / 2, 27, 0xFFE0E0E0);
        graphics.centeredText(font, Component.literal("Target: " + plan.workloadTargetValue().displayName()
                + " • internal render scale: " + Math.round(plan.workloadTargetValue().renderScale() * 100.0) + "%"),
                width / 2, 43, 0xFFE0E0E0);
        graphics.centeredText(font, Component.literal("Renderer: " + environment.rendererIdentity
                + " • Minecraft: " + environment.minecraftVersion), width / 2, 57, 0xFFB0B0B0);
        graphics.centeredText(font, Component.literal("Resolution: " + environment.displayResolution
                + " • VSync: " + environment.vsync + " • FPS cap: " + environment.fpsLimit), width / 2, 71, 0xFFB0B0B0);
        graphics.centeredText(font, Component.literal("Scenes: " + String.join(", ", plan.scenes)), width / 2, 85, 0xFFB0B0B0);
        graphics.centeredText(font, Component.literal("World: " + plan.requiredWorldName + " • seed: "
                + plan.expectedWorldSeed), width / 2, 99, 0xFFB0B0B0);
        graphics.centeredText(font, Component.literal(validation.message()), width / 2, 113,
                validation.valid() ? 0xFF70E070 : 0xFFFF7070);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().gui.setScreen(parent);
    }
}
