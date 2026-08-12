package com.minebench.capture;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/** Runtime counters are sampled at phase boundaries, never from the per-frame callback. */
public final class MemorySampler {
    private MemorySampler() {
    }

    public static RuntimeCounters read() {
        long heap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long collections = 0L;
        long pauseMillis = 0L;
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean bean : beans) {
            if (bean.getCollectionCount() > 0L) {
                collections += bean.getCollectionCount();
            }
            if (bean.getCollectionTime() > 0L) {
                pauseMillis += bean.getCollectionTime();
            }
        }
        return new RuntimeCounters(heap, collections, pauseMillis);
    }

    public record RuntimeCounters(long heapBytes, long gcCount, long gcPauseMillis) {
        public RuntimeCounters delta(RuntimeCounters before) {
            if (before == null) {
                return this;
            }
            return new RuntimeCounters(
                    heapBytes,
                    Math.max(0L, gcCount - before.gcCount),
                    Math.max(0L, gcPauseMillis - before.gcPauseMillis));
        }
    }
}
