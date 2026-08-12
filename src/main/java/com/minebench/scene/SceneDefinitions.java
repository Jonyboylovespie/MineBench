package com.minebench.scene;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class SceneDefinitions {
    private SceneDefinitions() {
    }

    public static List<SceneDescriptor> all() {
        return Arrays.stream(SceneId.values())
                .map(SceneDefinitions::descriptor)
                .toList();
    }

    public static Optional<SceneDescriptor> find(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Arrays.stream(SceneId.values())
                .filter(scene -> scene.id().equalsIgnoreCase(id))
                .map(SceneDefinitions::descriptor)
                .findFirst();
    }

    private static SceneDescriptor descriptor(SceneId scene) {
        return switch (scene) {
            case OVERWORLD_PANORAMA -> create(scene, "minecraft:overworld",
                    2048.5, 148.0, 2048.5, -135.0F, 24.0F, 8, "none", false);
            case OVERWORLD_FLYTHROUGH -> create(scene, "minecraft:overworld",
                    2048.5, 148.0, 1952.5, 0.0F, 12.0F, 10,
                    "minebench:camera/suite-1/overworld_flythrough.json", false);
            case NETHER_FLYTHROUGH -> create(scene, "minecraft:the_nether",
                    1024.5, 112.0, 928.5, 0.0F, 8.0F, 10,
                    "minebench:camera/suite-1/nether_flythrough.json", false);
            case END_FLYTHROUGH -> create(scene, "minecraft:the_end",
                    2048.5, 144.0, -95.5, 0.0F, 10.0F, 10,
                    "minebench:camera/suite-1/end_flythrough.json", false);
            case CHUNK_REBUILD -> create(scene, "minecraft:overworld",
                    2048.5, 148.0, 2048.5, -135.0F, 24.0F, 8, "none", true);
        };
    }

    private static SceneDescriptor create(SceneId scene, String dimension, double x, double y, double z,
                                          float yaw, float pitch, int radius, String route, boolean rebuild) {
        return new SceneDescriptor(scene.id(), scene.displayName(), scene.durationSeconds(), scene.mandatory(),
                description(scene), dimension, x, y, z, yaw, pitch, radius, route, rebuild);
    }

    private static String description(SceneId scene) {
        return switch (scene) {
            case OVERWORLD_PANORAMA -> "Fixed camera over fully warmed seeded Overworld terrain.";
            case OVERWORLD_FLYTHROUGH -> "Closed-loop route over seeded Overworld terrain at wall-clock speed.";
            case NETHER_FLYTHROUGH -> "Closed-loop route through seeded Nether terrain and atmosphere.";
            case END_FLYTHROUGH -> "Closed-loop route through seeded outer End islands and atmosphere.";
            case CHUNK_REBUILD -> "A full client geometry invalidation at the measurement boundary, followed by rebuild recovery.";
        };
    }
}
