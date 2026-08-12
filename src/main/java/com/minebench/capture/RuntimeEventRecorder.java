package com.minebench.capture;

import java.util.ArrayList;
import java.util.List;

/** Event recorder used by phase callbacks and contamination checks, not by the frame sampler. */
public final class RuntimeEventRecorder {
    private final List<RuntimeEvent> events = new ArrayList<>();

    public void record(String type, String message, long monotonicNanos) {
        events.add(new RuntimeEvent(type, message, monotonicNanos));
    }

    public List<RuntimeEvent> events() {
        return List.copyOf(events);
    }

    public void clear() {
        events.clear();
    }

    public record RuntimeEvent(String type, String message, long monotonicNanos) {
    }
}
