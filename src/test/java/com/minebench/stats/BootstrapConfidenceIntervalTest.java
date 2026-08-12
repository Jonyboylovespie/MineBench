package com.minebench.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BootstrapConfidenceIntervalTest {
    @Test
    void fixedSeedProducesDeterministicInterval() {
        double[] values = {100, 101, 102, 103, 104};
        ConfidenceInterval first = BootstrapConfidenceInterval.mean(values, 500, 42L, 0.95);
        ConfidenceInterval second = BootstrapConfidenceInterval.mean(values, 500, 42L, 0.95);
        assertTrue(first.available());
        assertEquals(first.lower(), second.lower());
        assertEquals(first.upper(), second.upper());
    }
}
