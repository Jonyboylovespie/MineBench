package com.minebench.benchmark;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BenchmarkPlanTest {
    @Test
    void unknownFieldsAreIgnoredAndInvalidValuesAreSanitized() {
        BenchmarkPlan plan = BenchmarkPlan.fromJson("""
                {"schemaVersion":99,"label":"  Test  ","workloadTarget":"GPU-bound","iterations":0,"scenes":["overworld_panorama","future_scene"],"future":true}
                """);
        assertEquals(1, plan.schemaVersion);
        assertEquals("Test", plan.label);
        assertEquals(1, plan.iterations);
        assertEquals(java.util.List.of("overworld_panorama"), plan.scenes);
        assertEquals(WorkloadTarget.GPU, plan.workloadTargetValue());
        assertEquals("gpu", plan.workloadTarget);
    }

    @Test
    void unknownWorkloadTargetsFallBackToBalanced() {
        BenchmarkPlan plan = BenchmarkPlan.fromJson("""
                {"workloadTarget":"future-accelerator","scenes":["overworld_panorama"]}
                """);
        assertEquals(WorkloadTarget.BALANCED, plan.workloadTargetValue());
        assertEquals(1.0, plan.workloadTargetValue().renderScale());
    }

    @Test
    void stateMachineRejectsIllegalTransitions() {
        BenchmarkStateMachine machine = new BenchmarkStateMachine();
        machine.transition(BenchmarkState.VALIDATING_ENVIRONMENT, "start", 1L);
        assertTrue(machine.transitions().size() == 1);
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> machine.transition(BenchmarkState.MEASURING, "skip", 2L));
    }

    @Test
    void completedStateCanStartAFreshSession() {
        BenchmarkStateMachine machine = new BenchmarkStateMachine();
        machine.transition(BenchmarkState.VALIDATING_ENVIRONMENT, "start", 1L);
        machine.transition(BenchmarkState.ABORTED, "test abort", 2L);
        machine.transition(BenchmarkState.RESTORING_SETTINGS, "restore", 3L);
        machine.transition(BenchmarkState.COMPLETE, "done", 4L);
        machine.transition(BenchmarkState.IDLE, "rerun", 5L);
        assertEquals(BenchmarkState.IDLE, machine.state());
    }
}
