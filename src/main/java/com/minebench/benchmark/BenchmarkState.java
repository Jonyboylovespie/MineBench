package com.minebench.benchmark;

/** Explicit benchmark lifecycle states. State changes happen outside the frame-sampling hot path. */
public enum BenchmarkState {
    IDLE,
    VALIDATING_ENVIRONMENT,
    SNAPSHOTTING_SETTINGS,
    PREPARING_WORLD,
    LOADING_SCENE,
    WAITING_FOR_CHUNKS,
    WAITING_FOR_RENDERER,
    WARMING_UP,
    SETTLING,
    MEASURING,
    COOLING_DOWN,
    PROCESSING_RESULTS,
    WRITING_REPORTS,
    SHOWING_RESULTS,
    RESTORING_SETTINGS,
    COMPLETE,
    ABORTED,
    FAILED
}
