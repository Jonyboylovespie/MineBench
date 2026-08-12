package com.minebench.benchmark;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ManagedPlanParser {
    private ManagedPlanParser() {
    }

    public static ManagedRunPlan read(Path file) throws IOException {
        return ManagedRunPlan.fromJson(Files.readString(file, StandardCharsets.UTF_8));
    }
}
