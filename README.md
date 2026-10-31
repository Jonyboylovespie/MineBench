# MineBench

MineBench is a client-side Fabric mod for deterministic Minecraft rendering benchmarks. It treats frame intervals as the primary data, keeps every iteration visible, records an environment fingerprint, and writes raw samples locally for independent verification.

Version 1.0.1 targets Minecraft Java Edition 26.3, Fabric, and Java 25. Benchmark methodology is versioned independently as Suite 1.

## What it measures

Suite 1 reports five separate workloads instead of inventing a universal score:

- Overworld static panorama — steady-state rendering at a fixed coordinate.
- Overworld flythrough — a fixed 60-second wall-clock route.
- Nether flythrough — the same route duration in seeded Nether terrain and atmosphere.
- End flythrough — the same route duration through seeded outer islands.
- Geometry rebuild — a full client geometry invalidation at the measurement boundary.

Quick mode runs one short health check. Standard mode defaults to five iterations. Extended mode runs the scene suite. Results include average FPS, mean/median/p90/p95/p99/p99.9 frame time, 1% low, optional 0.1% low, stutter thresholds, heap and GC deltas, per-iteration validity, variation, and a deterministic bootstrap interval when five valid iterations exist.

## Installation and development

Install Fabric Loader, Fabric API, and Java 25 for Minecraft 26.3. MineBench is client-only. Sodium, Iris, and Mod Menu are optional and are recorded when detected; they are not bundled.

```text
./gradlew build
```

The jar and sources jar are written to `build/libs/`. The testable benchmark core is in `src/main/java`; Minecraft-specific code is isolated in `src/client/java`.

## Running a benchmark

Open MineBench with F8 from the title screen and select **Create Suite 1 world**. MineBench creates an isolated spectator save named `MineBench Suite 1` with seed `7462910451`; publication runs refuse any other save or seed. The suite uses fixed anchors in the Overworld, Nether, and End. Weather and time progression are frozen and mob spawning is disabled during preparation.

Press F8 in that world, choose a mode, review preflight, and start. Advanced users can use:

```text
/minebench quick
/minebench standard
/minebench extended
/minebench abort
```

Reports are written to `minebench-results/YYYY-MM-DD_HH-mm-ss_<label>_<run-id>/` and contain:

- `summary.md` — human-readable results and methodology.
- `report.json` — versioned metadata, iteration statistics, transitions, and warnings.
- `frames.csv` — one row per retained raw frame interval.
- `environment.json` — sanitized environment fingerprint.
- `events.json` — phase transitions, contamination events, and invalidation evidence.
- `frame-times.svg` — post-run stutter histogram; never required to interpret the CSV.
- `complete.marker` — written last so partial directories are not treated as completed reports.

Quick mode is a one-iteration health check and is not evidence for a performance claim. Standard runs all five scenes five times. Extended runs all five scenes ten times with a longer warm-up. Setup, dimension travel, chunk loading, renderer readiness, and report writing stay outside the measured interval.

## Reproducible comparison workflow

Use separate launcher profiles with matching Minecraft, loader, API, renderer, resource packs, Java arguments, memory, resolution, and graphics settings. Prepare each profile once, then run at least five measured iterations in balanced or recorded randomized order. Compare each workload separately. Publish the complete report directory with any performance claim.

MineBench never uploads telemetry, swaps third-party jars, or silently changes third-party configuration. See [METHODOLOGY.md](METHODOLOGY.md) and [PRIVACY.md](PRIVACY.md).

## Project layout

```text
src/main/java/com/minebench/       pure benchmark core and report formats
src/client/java/com/minebench/     Fabric client adapter and screens
src/main/resources/schemas/        managed-plan and report schemas
examples/                          managed plan and report guidance
scripts/                            opt-in external orchestration helper
```

The public project name and Modrinth/project-directory collision check must be repeated before release.
