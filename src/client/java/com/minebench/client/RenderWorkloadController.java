package com.minebench.client;

import com.minebench.benchmark.WorkloadTarget;
import com.minebench.benchmark.RenderResolution;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.renderer.state.WindowRenderState;

/** Applies a benchmark-only internal render scale and enables Minecraft's asynchronous GPU timer. */
public final class RenderWorkloadController {
    private static WorkloadTarget target = WorkloadTarget.BALANCED;
    private static boolean active;
    private static DebugScreenEntryStatus previousGpuTimerStatus;

    private RenderWorkloadController() {
    }

    public static void begin(Minecraft client, WorkloadTarget requested) {
        target = requested == null ? WorkloadTarget.BALANCED : requested;
        active = true;
        if (previousGpuTimerStatus == null) {
            previousGpuTimerStatus = client.debugEntries.getStatus(DebugScreenEntries.GPU_UTILIZATION);
            client.debugEntries.setStatus(DebugScreenEntries.GPU_UTILIZATION, DebugScreenEntryStatus.ALWAYS_ON);
        }
    }

    public static void end(Minecraft client) {
        active = false;
        target = WorkloadTarget.BALANCED;
        if (previousGpuTimerStatus != null) {
            client.debugEntries.setStatus(DebugScreenEntries.GPU_UTILIZATION, previousGpuTimerStatus);
            previousGpuTimerStatus = null;
        }
        // The following vanilla extraction sees native dimensions and resizes all targets back without persisting
        // any option or changing the physical window.
    }

    public static void apply(WindowRenderState state) {
        if (!active || state == null) return;
        int maxTextureSize;
        try {
            maxTextureSize = RenderSystem.getDevice().getDeviceInfo().limits().maxTextureSize();
        } catch (RuntimeException | LinkageError ignored) {
            maxTextureSize = 16_384;
        }
        RenderResolution resolution = RenderResolution.scaled(
                state.width, state.height, target.renderScale(), maxTextureSize);
        state.width = resolution.width();
        state.height = resolution.height();
    }

    public static WorkloadTarget target() {
        return target;
    }

    public static boolean active() {
        return active;
    }

}
