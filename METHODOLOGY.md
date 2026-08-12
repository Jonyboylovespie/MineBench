# MineBench methodology — Suite 1

This document defines the statistics and validity rules used by MineBench 1.0.0. A methodology, seed, coordinate, route, or workload change increments the suite version.

## Suite 1 world and workloads

Publication runs use the dedicated `MineBench Suite 1` save with seed `7462910451`. MineBench creates the save in spectator mode and rejects a different level name, seed, remote server, or LAN-published session. Each workload records its dimension, exact eye-coordinate anchor, readiness radius, camera resource/hash, and complete scene-contract hash.

- `overworld_panorama`: 30-second fixed view at `(2048.5, 148.0, 2048.5)`.
- `overworld_flythrough`: 60-second closed route centered on the same seeded Overworld region.
- `nether_flythrough`: 60-second closed route centered near `(1024.5, 88.0, 1024.5)`.
- `end_flythrough`: 60-second closed route centered near `(2048.5, 120.0, 0.5)`.
- `chunk_rebuild`: 30 seconds beginning immediately after full client geometry invalidation at the Overworld panorama.

The three flythrough routes are driven by monotonic wall-clock time, not rendered frames or client ticks. Time/weather progression and hostile/passive spawning are disabled during scene preparation to reduce temporal variance. MineBench does not place or remove terrain blocks.

## Timing and sampling

MineBench records one timestamp at the tail of the Minecraft 26.2 `Minecraft.renderFrame(boolean)` method. The callback is active only for a loaded world with no open screen and only in `MEASURING`. `System.nanoTime()` supplies a monotonic clock. The first timestamp is an anchor; it is not treated as an ordinary frame interval. Every subsequent interval is stored in a fixed preallocated `long[]` as nanoseconds.

The measured duration is `lastAcceptedTimestamp - firstAcceptedTimestamp`. No report writing, logging, JSON/CSV encoding, sorting, UI rebuilding, collection growth, or network I/O occurs in the sampling callback. Sorting and encoding happen after measurement. If the fixed buffer overflows, aggregate counters continue but raw-data-dependent percentile results are flagged incomplete and the iteration is invalid by default.

## Frame statistics

For `n` accepted intervals and measured duration `D` seconds:

- Average FPS = `n / D`.
- Mean frame time = arithmetic mean of the accepted intervals in milliseconds.
- p50, p90, p95, p99, and p99.9 use a sorted copy and linear interpolation at `p * (n - 1)`.
- 1% low FPS = `1000 / mean(slowest ceil(0.01 * n) intervals in milliseconds)`.
- 0.1% low FPS uses the same rule and is shown only when at least 1,000 intervals exist.
- Stutter counters count intervals strictly greater than 16.67 ms, 33.33 ms, 50 ms, and 100 ms.
- `1000 / average FPS` is not reported as an independently measured frame-time metric.

Heap usage and garbage-collector count/pause deltas are sampled at measured-phase boundaries. GC activity is part of the result and is reported rather than silently excluded.

## Repeated runs

Each scene keeps every iteration, including slow or invalid iterations. The aggregate headline values are medians of valid iterations: median average FPS, median 1% low FPS, and median p99 frame time. It also reports minimum, maximum, median absolute deviation (MAD), coefficient of variation (population standard deviation divided by the mean), and counts of valid/invalid/aborted iterations. A deterministic 95% percentile bootstrap interval for average FPS is available when there are at least five valid iterations.

One-run quick results are labeled as single runs and do not claim a confidence estimate.

## Comparisons

For baseline A and candidate B:

- FPS delta = `B - A`.
- FPS percentage change = `(B - A) / A * 100`.
- Frame-time percentage change uses the selected statistic, normally p99: `(B - A) / A * 100`.

MineBench warns when the observed FPS delta is no larger than baseline MAD or when either side has fewer than five valid iterations. Use workload-specific language such as “higher median average FPS in this workload,” not a universal “faster overall” score.

## Preparation and readiness

World creation, scene loading, dimension travel, resource/shader compilation, chunk readiness, route warm-up, settling, and report processing are outside the measured interval. MineBench waits for every full client chunk in the scene radius, then requires Minecraft to report all visible sections rendered for 20 consecutive client ticks. A 180-second preparation deadline fails the run rather than silently measuring incomplete terrain.

Standard defaults are a 20-second warm-up, five-second settling window, 180-second preparation timeout, and 180-second warm-up timeout. Extended mode uses a 60-second warm-up. A warm-up timeout remains visible as invalid evidence; a preparation timeout stops the benchmark.

## Validity

The following invalidate an iteration by default: focus loss, minimization, any active Minecraft framerate-throttle reason, resize/framebuffer changes, fullscreen changes, pause menu, resource reload, shader recompilation, relevant setting changes, world/dimension changes, camera deviation, raw buffer overflow, renderer/readiness timeout, and interruption. While a benchmark is active, MineBench refreshes Minecraft's input-activity timer because scripted camera movement is not treated as player input; this prevents the vanilla AFK limiter from introducing 30 FPS and 10 FPS caps without changing the user's saved inactivity option. Extreme frame intervals remain samples unless linked to a recorded invalidating event. Invalid runs remain on disk for diagnosis but are excluded from headline medians by default.

## Environment matching

Reports include mod versions, renderer/shader/resource-pack state and shaderpack SHA-256 when available, display/framebuffer and graphics settings, operating system/architecture, Java/JVM/heap/GC data, OpenGL vendor/renderer/version, CPU model/count, world seed/name, scene coordinates, and route/scene hashes. Missing data is written as `unavailable`. Comparisons must disclose mismatches and require explicit acknowledgement before being treated as comparable.

## Development frame-boundary decision

For 26.2, MineBench uses a required mixin at `Minecraft.renderFrame(boolean)` and verifies the callback against an independent render counter. The method descriptor is version-pinned; any Minecraft upgrade must revalidate it before release.
