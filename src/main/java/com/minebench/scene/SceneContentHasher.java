package com.minebench.scene;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class SceneContentHasher {
    private SceneContentHasher() {
    }

    public static String sha256(SceneManifest manifest) {
        if (manifest == null) return "unavailable";
        StringBuilder canonical = new StringBuilder()
                .append(manifest.schemaVersion).append('|')
                .append(manifest.suiteVersion).append('|')
                .append(manifest.sceneId).append('|')
                .append(manifest.seed).append('|')
                .append(manifest.dimension).append('|')
                .append(Double.toString(manifest.anchorX)).append('|')
                .append(Double.toString(manifest.anchorY)).append('|')
                .append(Double.toString(manifest.anchorZ)).append('|')
                .append(Float.toString(manifest.yaw)).append('|')
                .append(Float.toString(manifest.pitch)).append('|')
                .append(manifest.readinessRadiusChunks).append('|')
                .append(manifest.cameraResource).append('|')
                .append(manifest.geometryRebuild).append('|')
                .append(manifest.routeHash);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required", exception);
        }
    }

}
