package com.minebench.environment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentComparatorTest {
    @Test
    void rendererAndResolutionMismatchBlocksComparison() {
        EnvironmentFingerprint left = EnvironmentFingerprint.unavailable();
        EnvironmentFingerprint right = EnvironmentFingerprint.unavailable();
        left.rendererIdentity = "standard renderer";
        right.rendererIdentity = "Sodium";
        EnvironmentComparison comparison = EnvironmentComparator.compare(left, right);
        assertFalse(comparison.compatible());
        assertTrue(comparison.differences().stream().anyMatch(difference -> difference.field().equals("rendererIdentity")));
    }
}
