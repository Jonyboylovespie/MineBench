package com.minebench.client;

import com.minebench.camera.CameraPath;
import com.minebench.camera.CameraSample;
import com.minebench.scene.SceneDescriptor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Applies a versioned MineBench route to the local player and restores every touched camera/movement field. */
public final class ClientCameraRoute {
    private CameraPath path;
    private Entity savedCameraEntity;
    private net.minecraft.world.phys.Vec3 savedPosition;
    private float savedYRot;
    private float savedXRot;
    private float savedHeadRot;
    private boolean savedNoPhysics;
    private boolean savedNoGravity;
    private boolean savedFlying;
    private boolean active;
    private long routeStartNanos;

    public boolean start(Minecraft client, SceneDescriptor scene, long now) {
        if (active || client.player == null) return false;
        if (scene == null || !scene.hasCameraRoute()) return false;
        try {
            path = load(client, scene);
        } catch (IOException | RuntimeException exception) {
            System.err.println("[MineBench] Could not load scripted camera route: " + exception.getMessage());
            path = null;
            return false;
        }
        LocalPlayer player = client.player;
        savedCameraEntity = client.getCameraEntity();
        savedPosition = player.position();
        savedYRot = player.getYRot();
        savedXRot = player.getXRot();
        savedHeadRot = player.getYHeadRot();
        savedNoPhysics = player.noPhysics;
        savedNoGravity = player.isNoGravity();
        savedFlying = player.getAbilities().flying;
        active = true;
        routeStartNanos = now;
        client.setCameraEntity(player);
        apply(client, 0.0);
        return true;
    }

    /** Locks a static-scene camera at its versioned suite coordinate. */
    public boolean lockAt(Minecraft client, SceneDescriptor scene, long now) {
        if (scene == null) return false;
        if (active) {
            if (path != null) stop(client);
            else return true;
        }
        if (client.player == null) return false;
        LocalPlayer player = client.player;
        savedCameraEntity = client.getCameraEntity();
        savedPosition = player.position();
        savedYRot = player.getYRot();
        savedXRot = player.getXRot();
        savedHeadRot = player.getYHeadRot();
        savedNoPhysics = player.noPhysics;
        savedNoGravity = player.isNoGravity();
        savedFlying = player.getAbilities().flying;
        active = true;
        path = null;
        routeStartNanos = now;
        client.setCameraEntity(player);
        player.setPos(scene.anchorX(), scene.anchorY() - player.getEyeHeight(), scene.anchorZ());
        player.setYRot(scene.yaw());
        player.setXRot(scene.pitch());
        player.setYHeadRot(scene.yaw());
        player.setYBodyRot(scene.yaw());
        applyLock(player);
        return true;
    }

    public void update(Minecraft client, long now) {
        if (!active) return;
        if (path == null) {
            if (client.player != null) applyLock(client.player);
        } else {
            apply(client, Math.max(0.0, (now - routeStartNanos) / 1_000_000_000.0));
        }
    }

    public void restart(long now) {
        routeStartNanos = now;
    }

    public void stop(Minecraft client) {
        if (!active) return;
        LocalPlayer player = client.player;
        if (player != null && savedPosition != null) {
            player.setPos(savedPosition);
            player.setYRot(savedYRot);
            player.setXRot(savedXRot);
            player.setYHeadRot(savedHeadRot);
            player.setYBodyRot(savedHeadRot);
            player.noPhysics = savedNoPhysics;
            player.setNoGravity(savedNoGravity);
            player.getAbilities().flying = savedFlying;
            player.setDeltaMovement(0.0, 0.0, 0.0);
        }
        if (savedCameraEntity != null) client.setCameraEntity(savedCameraEntity);
        active = false;
        path = null;
    }

    public boolean active() {
        return active;
    }

    public String pathId() {
        return path == null ? "unavailable" : path.id();
    }

    public String routeHash() {
        return path == null ? "unavailable" : path.routeHash();
    }

    public static CameraPath load(Minecraft client, SceneDescriptor scene) throws IOException {
        if (scene == null || !scene.hasCameraRoute()) return null;
        Identifier resourceId = Identifier.parse(scene.cameraResource());
        try (InputStream stream = client.getResourceManager().getResourceOrThrow(resourceId).open()) {
            return CameraPath.fromJson(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private void apply(Minecraft client, double elapsedSeconds) {
        LocalPlayer player = client.player;
        if (player == null || path == null) return;
        CameraSample sample = path.sampleAtSeconds(elapsedSeconds);
        com.minebench.camera.Vec3 position = sample.position();
        // Keyframes describe camera-eye coordinates; a player position is one eye-height below them.
        player.setPos(position.x(), position.y() - player.getEyeHeight(), position.z());
        player.setYRot((float) sample.orientation().yawDegrees());
        player.setXRot((float) -sample.orientation().pitchDegrees());
        player.setYHeadRot(player.getYRot());
        player.setYBodyRot(player.getYRot());
        player.setDeltaMovement(0.0, 0.0, 0.0);
        player.noPhysics = true;
        player.setNoGravity(true);
    }

    private static void applyLock(LocalPlayer player) {
        player.setDeltaMovement(0.0, 0.0, 0.0);
        player.noPhysics = true;
        player.setNoGravity(true);
    }
}
