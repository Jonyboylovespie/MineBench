package com.minebench.scene;

/** Immutable contract for one seeded-world workload. */
public final class SceneManifest {
    public int schemaVersion = 1;
    public String suiteVersion = "1";
    public String sceneId;
    public long seed;
    public String dimension = "unavailable";
    public double anchorX;
    public double anchorY;
    public double anchorZ;
    public float yaw;
    public float pitch;
    public int readinessRadiusChunks;
    public String cameraResource = "none";
    public boolean geometryRebuild;
    public String contentHash = "unavailable";
    public String routeHash = "unavailable";

    public SceneManifest copy() {
        SceneManifest copy = new SceneManifest();
        copy.schemaVersion = schemaVersion;
        copy.suiteVersion = suiteVersion;
        copy.sceneId = sceneId;
        copy.seed = seed;
        copy.dimension = dimension;
        copy.anchorX = anchorX;
        copy.anchorY = anchorY;
        copy.anchorZ = anchorZ;
        copy.yaw = yaw;
        copy.pitch = pitch;
        copy.readinessRadiusChunks = readinessRadiusChunks;
        copy.cameraResource = cameraResource;
        copy.geometryRebuild = geometryRebuild;
        copy.contentHash = contentHash;
        copy.routeHash = routeHash;
        return copy;
    }
}
