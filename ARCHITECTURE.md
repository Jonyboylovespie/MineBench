# MineBench architecture decisions

## ADR-0001: keep the measured loop primitive-only

Frame sampling writes timestamps and flags to preallocated primitive buffers. The sampler cannot write files, format strings, sort, grow collections, or rebuild UI. Post-processing owns all allocations and report generation.

## ADR-0002: use a single state machine

`BenchmarkController` owns phase transitions, cancellation, iteration sequencing, validity, and report input. Client callbacks only provide readiness/frame/tick events and never implement independent phase logic.

## ADR-0003: use the version-pinned client frame boundary

`Minecraft.renderFrame(boolean)` is the selected 26.2 boundary. The mixin is required and invokes the sampler once at method tail. The client adapter requires a loaded world and no open screen, so menus and loading overlays cannot become samples. This hook must be revalidated for every Minecraft mapping update.

## ADR-0004: preserve invalid evidence

Contamination creates a warning and invalidates the iteration by policy; it never deletes raw data. Aggregates exclude invalid iterations by default and retain their reason codes in `events.json` and `report.json`.

## ADR-0005: renderer-neutral optional integrations

Sodium, Iris, Mod Menu, GPU timers, and renderer-specific readiness are optional adapters. Their absence does not change the core formulas. Their versions and unavailable fields are visible in the environment fingerprint.

## ADR-0006: use an isolated seeded vanilla world

Suite 1 benchmarks ordinary seeded terrain rather than placing a synthetic arena. The client creates a dedicated spectator save, verifies its name and seed before every run, teleports through fixed dimension anchors, and never mutates blocks. This preserves visual relevance while protecting unrelated user saves.
