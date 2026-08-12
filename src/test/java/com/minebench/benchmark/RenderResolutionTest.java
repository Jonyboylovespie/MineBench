package com.minebench.benchmark;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RenderResolutionTest {
    @Test
    void scalesBothAxesForCpuAndGpuTargets() {
        assertEquals(new RenderResolution(960, 540),
                RenderResolution.scaled(1920, 1080, WorkloadTarget.CPU.renderScale(), 16_384));
        assertEquals(new RenderResolution(3840, 2160),
                RenderResolution.scaled(1920, 1080, WorkloadTarget.GPU.renderScale(), 16_384));
    }

    @Test
    void clampsToDeviceTextureLimitAndRejectsInvalidDimensions() {
        assertEquals(new RenderResolution(4096, 4096),
                RenderResolution.scaled(4000, 3000, 2.0, 4096));
        assertEquals(new RenderResolution(1, 1),
                RenderResolution.scaled(0, -2, Double.NaN, 4096));
    }
}
