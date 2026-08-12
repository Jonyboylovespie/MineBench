package com.minebench.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.minebench.benchmark.BenchmarkState;

public final class NoticeScreen extends Screen {
    private final Screen parent;
    private final String message;

    public NoticeScreen(Screen parent, String title, String message) {
        super(Component.literal(title == null ? "MineBench" : title));
        this.parent = parent;
        this.message = message == null ? "" : message;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(width / 2 - 60, height - 35, 120, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xD0101010);
        graphics.centeredText(font, title, width / 2, height / 2 - 28, 0xFFFFFFFF);
        graphics.centeredText(font, Component.literal(message), width / 2, height / 2, 0xFFCCCCCC);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (MineBenchClient.controller() != null) {
            BenchmarkState state = MineBenchClient.controller().core().state();
            if (state == BenchmarkState.ABORTED || state == BenchmarkState.FAILED) {
                MineBenchClient.controller().core().beginRestoration(System.nanoTime());
                MineBenchClient.controller().core().complete(System.nanoTime());
            }
        }
        Minecraft.getInstance().gui.setScreen(parent);
    }
}
