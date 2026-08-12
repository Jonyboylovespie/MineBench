package com.minebench.scene;

public record SceneDescriptor(
        String id,
        String displayName,
        int durationSeconds,
        boolean mandatory,
        String workloadDescription,
        String dimension,
        double anchorX,
        double anchorY,
        double anchorZ,
        float yaw,
        float pitch,
        int readinessRadiusChunks,
        String cameraResource,
        boolean geometryRebuild
) {
    public String anchorDescription() {
        return String.format(java.util.Locale.ROOT, "%.1f, %.1f, %.1f", anchorX, anchorY, anchorZ);
    }

    public boolean hasCameraRoute() {
        return cameraResource != null && !cameraResource.isBlank() && !"none".equals(cameraResource);
    }
}
