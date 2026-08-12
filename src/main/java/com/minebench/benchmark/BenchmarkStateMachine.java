package com.minebench.benchmark;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * The single source of truth for legal benchmark transitions. It intentionally has no Minecraft dependency so every
 * transition can be tested without launching a client.
 */
public final class BenchmarkStateMachine {
    private static final Map<BenchmarkState, EnumSet<BenchmarkState>> ALLOWED = createAllowedTransitions();

    private BenchmarkState state = BenchmarkState.IDLE;
    private final List<BenchmarkTransition> transitions = new ArrayList<>();

    public BenchmarkState state() {
        return state;
    }

    public List<BenchmarkTransition> transitions() {
        return List.copyOf(transitions);
    }

    public BenchmarkTransition transition(BenchmarkState next, String reason, long monotonicNanos) {
        if (next == null) {
            throw new IllegalArgumentException("next state cannot be null");
        }
        if (!ALLOWED.getOrDefault(state, EnumSet.noneOf(BenchmarkState.class)).contains(next)) {
            throw new IllegalStateException("Illegal MineBench transition: " + state + " -> " + next);
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Every transition needs a reason");
        }
        BenchmarkTransition transition = new BenchmarkTransition(state, next, reason, monotonicNanos);
        state = next;
        transitions.add(transition);
        return transition;
    }

    public void reset() {
        if (state != BenchmarkState.IDLE) {
            throw new IllegalStateException("Only an idle state machine can be reset");
        }
        transitions.clear();
    }

    private static Map<BenchmarkState, EnumSet<BenchmarkState>> createAllowedTransitions() {
        Map<BenchmarkState, EnumSet<BenchmarkState>> allowed = new EnumMap<>(BenchmarkState.class);
        allow(allowed, BenchmarkState.IDLE, BenchmarkState.VALIDATING_ENVIRONMENT);
        allow(allowed, BenchmarkState.VALIDATING_ENVIRONMENT,
                BenchmarkState.SNAPSHOTTING_SETTINGS, BenchmarkState.ABORTED, BenchmarkState.FAILED);
        allow(allowed, BenchmarkState.SNAPSHOTTING_SETTINGS,
                BenchmarkState.PREPARING_WORLD, BenchmarkState.ABORTED, BenchmarkState.FAILED);
        allow(allowed, BenchmarkState.PREPARING_WORLD,
                BenchmarkState.LOADING_SCENE, BenchmarkState.ABORTED, BenchmarkState.FAILED);
        allow(allowed, BenchmarkState.LOADING_SCENE,
                BenchmarkState.WAITING_FOR_CHUNKS, BenchmarkState.ABORTED, BenchmarkState.FAILED);
        allow(allowed, BenchmarkState.WAITING_FOR_CHUNKS,
                BenchmarkState.WAITING_FOR_RENDERER, BenchmarkState.ABORTED, BenchmarkState.FAILED);
        allow(allowed, BenchmarkState.WAITING_FOR_RENDERER,
                BenchmarkState.WARMING_UP, BenchmarkState.ABORTED, BenchmarkState.FAILED);
        allow(allowed, BenchmarkState.WARMING_UP,
                BenchmarkState.SETTLING, BenchmarkState.ABORTED, BenchmarkState.FAILED);
        allow(allowed, BenchmarkState.SETTLING,
                BenchmarkState.MEASURING, BenchmarkState.ABORTED, BenchmarkState.FAILED);
        allow(allowed, BenchmarkState.MEASURING,
                BenchmarkState.COOLING_DOWN, BenchmarkState.ABORTED, BenchmarkState.FAILED);
        allow(allowed, BenchmarkState.COOLING_DOWN,
                BenchmarkState.PROCESSING_RESULTS, BenchmarkState.ABORTED, BenchmarkState.FAILED);
        allow(allowed, BenchmarkState.PROCESSING_RESULTS,
                BenchmarkState.LOADING_SCENE, BenchmarkState.SETTLING,
                BenchmarkState.WRITING_REPORTS, BenchmarkState.ABORTED, BenchmarkState.FAILED);
        allow(allowed, BenchmarkState.WRITING_REPORTS,
                BenchmarkState.SHOWING_RESULTS, BenchmarkState.RESTORING_SETTINGS,
                BenchmarkState.ABORTED, BenchmarkState.FAILED);
        allow(allowed, BenchmarkState.SHOWING_RESULTS,
                BenchmarkState.RESTORING_SETTINGS, BenchmarkState.COMPLETE);
        allow(allowed, BenchmarkState.RESTORING_SETTINGS,
                BenchmarkState.COMPLETE, BenchmarkState.FAILED);
        allow(allowed, BenchmarkState.COMPLETE, BenchmarkState.IDLE);
        allow(allowed, BenchmarkState.ABORTED, BenchmarkState.RESTORING_SETTINGS, BenchmarkState.COMPLETE);
        allow(allowed, BenchmarkState.FAILED, BenchmarkState.RESTORING_SETTINGS, BenchmarkState.COMPLETE);
        return Map.copyOf(allowed);
    }

    private static void allow(Map<BenchmarkState, EnumSet<BenchmarkState>> allowed,
                              BenchmarkState from,
                              BenchmarkState... targets) {
        EnumSet<BenchmarkState> states = EnumSet.noneOf(BenchmarkState.class);
        for (BenchmarkState target : targets) {
            states.add(target);
        }
        allowed.put(from, states);
    }
}
