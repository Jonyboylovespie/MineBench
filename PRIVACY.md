# MineBench privacy

MineBench is local by default. It writes reports under the Minecraft instance’s `minebench-results` directory and does not upload telemetry.

Reports intentionally include technical benchmarking context: Minecraft/Fabric/mod versions, renderer and shader state when available, resource-pack names/order, display and graphics settings, operating system and Java details, JVM arguments after path redaction, heap/GC information, GPU strings when an adapter is enabled, CPU architecture/count, benchmark seed, scene/path hashes, and timing metadata.

MineBench does not collect account names, UUIDs, access tokens, server addresses, worlds outside the MineBench scope, or arbitrary absolute file paths. JVM arguments are sanitized before export. Optional integration metadata is supplied by the user’s installed mods and should be reviewed before publication.

There is no upload feature in 1.0.0. Any future upload component must be explicit opt-in, show a preview of fields, and keep local raw files regardless of upload outcome.
