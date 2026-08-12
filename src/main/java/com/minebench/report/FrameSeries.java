package com.minebench.report;

import com.minebench.capture.PrimitiveSampleBuffer;

public record FrameSeries(String sceneId, int iteration, PrimitiveSampleBuffer.Snapshot samples) {
}
