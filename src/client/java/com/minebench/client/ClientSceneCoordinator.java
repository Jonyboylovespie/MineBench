package com.minebench.client;

import com.minebench.scene.SceneDescriptor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.clock.WorldClocks;

import java.util.Set;
import java.util.UUID;

/** Coordinates deterministic suite locations with the integrated server; never runs from the frame callback. */
public final class ClientSceneCoordinator {
    private SceneDescriptor pendingScene;
    private Snapshot original;
    private long preparationStartedNanos;
    private boolean teleportRequested;
    private boolean geometryInvalidated;
    private int rendererReadyTicks;

    public Validation validateWorld(Minecraft client, long expectedSeed, String requiredName, boolean requireDedicated) {
        var server = client.getSingleplayerServer();
        if (server == null || client.player == null || client.level == null) {
            return new Validation(false, "MineBench publication runs require a local single-player world");
        }
        long actualSeed = server.getWorldGenSettings().options().seed();
        if (actualSeed != expectedSeed) {
            return new Validation(false, "World seed mismatch: expected " + expectedSeed + ", found " + actualSeed);
        }
        String actualName = server.getWorldData().getLevelName();
        if (requireDedicated && !requiredName.equals(actualName)) {
            return new Validation(false, "Open the dedicated world named '" + requiredName + "' (current: '" + actualName + "')");
        }
        if (server.isPublished()) {
            return new Validation(false, "Close LAN publishing before benchmarking");
        }
        return new Validation(true, "Dedicated benchmark world and seed verified");
    }

    public void captureOriginal(Minecraft client) {
        if (original != null || client.player == null || client.level == null) return;
        original = new Snapshot(client.player.getUUID(), client.level.dimension(), client.player.getX(),
                client.player.getY(), client.player.getZ(), client.player.getYRot(), client.player.getXRot());
    }

    public boolean begin(Minecraft client, SceneDescriptor scene, long now) {
        if (scene == null || client.player == null || client.getSingleplayerServer() == null) return false;
        pendingScene = scene;
        preparationStartedNanos = now;
        teleportRequested = true;
        geometryInvalidated = false;
        rendererReadyTicks = 0;
        UUID playerId = client.player.getUUID();
        double feetY = scene.anchorY() - client.player.getEyeHeight();
        ResourceKey<Level> target = dimension(scene.dimension());
        client.getSingleplayerServer().execute(() -> {
            var server = client.getSingleplayerServer();
            server.getGameRules().set(GameRules.ADVANCE_TIME, false, server);
            server.getGameRules().set(GameRules.ADVANCE_WEATHER, false, server);
            server.getGameRules().set(GameRules.SPAWN_MOBS, false, server);
            server.getGameRules().set(GameRules.SPAWN_MONSTERS, false, server);
            server.getGameRules().set(GameRules.SPECTATORS_GENERATE_CHUNKS, true, server);
            server.setWeatherParameters(1_000_000, 0, false, false);
            var clocks = server.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK);
            server.overworld().clockManager().setTotalTicks(clocks.getOrThrow(WorldClocks.OVERWORLD), 6_000L);
            server.overworld().clockManager().setTotalTicks(clocks.getOrThrow(WorldClocks.THE_END), 6_000L);
            ServerPlayer player = client.getSingleplayerServer().getPlayerList().getPlayer(playerId);
            ServerLevel level = client.getSingleplayerServer().getLevel(target);
            if (player != null && level != null) {
                player.teleportTo(level, scene.anchorX(), feetY, scene.anchorZ(), Set.of(), scene.yaw(), scene.pitch(), true);
            }
        });
        return true;
    }

    public boolean chunksReady(Minecraft client) {
        if (!atTarget(client) || pendingScene == null) return false;
        int centerX = SectionPos.blockToSectionCoord(pendingScene.anchorX());
        int centerZ = SectionPos.blockToSectionCoord(pendingScene.anchorZ());
        int radius = pendingScene.readinessRadiusChunks();
        for (int z = centerZ - radius; z <= centerZ + radius; z++) {
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                if (client.level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false) == null) return false;
            }
        }
        return true;
    }

    public boolean rendererReady(Minecraft client) {
        if (!chunksReady(client) || client.player == null) return false;
        if (!client.levelRenderer.hasRenderedAllSections()) {
            rendererReadyTicks = 0;
            return false;
        }
        return ++rendererReadyTicks >= 20;
    }

    public void invalidateGeometry(Minecraft client) {
        if (geometryInvalidated || client.level == null) return;
        client.levelRenderer.invalidateCompiledGeometry(client.level, client.options,
                client.gameRenderer.mainCamera(), client.getBlockColors());
        geometryInvalidated = true;
    }

    public void resetIterationWorkload() {
        geometryInvalidated = false;
    }

    public long preparationStartedNanos() {
        return preparationStartedNanos;
    }

    public boolean teleportRequested() {
        return teleportRequested;
    }

    public SceneDescriptor pendingScene() {
        return pendingScene;
    }

    public void restoreOriginal(Minecraft client) {
        if (original == null || client.getSingleplayerServer() == null) return;
        Snapshot snapshot = original;
        client.getSingleplayerServer().executeBlocking(() -> {
            ServerPlayer player = client.getSingleplayerServer().getPlayerList().getPlayer(snapshot.playerId());
            ServerLevel level = client.getSingleplayerServer().getLevel(snapshot.dimension());
            if (player != null && level != null) {
                player.teleportTo(level, snapshot.x(), snapshot.y(), snapshot.z(), Set.of(),
                        snapshot.yaw(), snapshot.pitch(), true);
            }
        });
        original = null;
    }

    private boolean atTarget(Minecraft client) {
        if (pendingScene == null || client.level == null || client.player == null) return false;
        if (client.level.dimension() != dimension(pendingScene.dimension())) return false;
        double dx = client.player.getX() - pendingScene.anchorX();
        double dz = client.player.getZ() - pendingScene.anchorZ();
        return dx * dx + dz * dz <= 16.0;
    }

    private static ResourceKey<Level> dimension(String id) {
        return switch (id) {
            case "minecraft:the_nether" -> Level.NETHER;
            case "minecraft:the_end" -> Level.END;
            default -> Level.OVERWORLD;
        };
    }

    public record Validation(boolean valid, String message) {}

    private record Snapshot(UUID playerId, ResourceKey<Level> dimension, double x, double y, double z,
                            float yaw, float pitch) {}
}
