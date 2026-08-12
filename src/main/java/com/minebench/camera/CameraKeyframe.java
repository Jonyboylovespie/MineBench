package com.minebench.camera;

public record CameraKeyframe(double timeSeconds, Vec3 position, Quaternion orientation) {
    public CameraKeyframe {
        if (!Double.isFinite(timeSeconds) || timeSeconds < 0.0) {
            throw new IllegalArgumentException("Camera keyframe time must be finite and non-negative");
        }
        if (position == null || orientation == null) {
            throw new IllegalArgumentException("Camera keyframe needs position and orientation");
        }
        orientation = orientation.normalized();
    }
}
