package com.minebench.camera;

/** Pure route session model; the client adapter applies its sample to the Minecraft camera/player state. */
public final class CameraController {
    private CameraPath path;
    private CameraSample savedState;
    private CameraSample currentState;
    private boolean active;

    public void start(CameraPath nextPath, CameraSample initialState) {
        if (nextPath == null || initialState == null) throw new IllegalArgumentException("Camera route and state required");
        path = nextPath;
        savedState = initialState;
        currentState = initialState;
        active = true;
    }

    public CameraSample updateFromElapsedSeconds(double elapsedSeconds) {
        if (!active) return currentState;
        currentState = path.sampleAtSeconds(elapsedSeconds);
        return currentState;
    }

    public CameraSample stop() {
        active = false;
        currentState = savedState;
        return savedState;
    }

    public boolean active() {
        return active;
    }

    public CameraPath path() {
        return path;
    }

    public CameraSample currentState() {
        return currentState;
    }
}
