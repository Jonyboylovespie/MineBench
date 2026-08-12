package com.minebench.client;

import com.minebench.benchmark.BenchmarkPlan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import java.nio.file.Files;
import java.nio.file.Path;

/** Creates or opens the isolated seeded save used by the published suite. */
public final class BenchmarkWorldManager {
    public static final String WORLD_FOLDER = "MineBench_Suite_1";

    private BenchmarkWorldManager() {
    }

    public static boolean exists(Minecraft client) {
        return Files.isRegularFile(worldPath(client).resolve("level.dat"));
    }

    public static Path worldPath(Minecraft client) {
        return client.gameDirectory.toPath().resolve("saves").resolve(WORLD_FOLDER);
    }

    public static void createOrOpen(Minecraft client, BenchmarkPlan plan, Screen parent) {
        if (client.level != null) {
            client.gui.setScreen(new NoticeScreen(parent, "Return to the title screen",
                    "MineBench will not leave or modify the world you currently have open."));
            return;
        }
        plan.sanitize();
        if (exists(client)) {
            client.createWorldOpenFlows().openWorld(WORLD_FOLDER, () ->
                    client.gui.setScreen(new NoticeScreen(parent, "Could not open benchmark world",
                            "Check the game log for the world-loading error.")));
            return;
        }
        LevelSettings settings = new LevelSettings(plan.requiredWorldName, GameType.SPECTATOR,
                new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, true), true,
                WorldDataConfiguration.DEFAULT);
        WorldOptions options = new WorldOptions(plan.expectedWorldSeed, true, false);
        client.createWorldOpenFlows().createFreshLevel(WORLD_FOLDER, settings, options,
                WorldPresets::createNormalWorldDimensions, parent);
    }
}
