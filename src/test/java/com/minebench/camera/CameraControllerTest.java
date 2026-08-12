package com.minebench.camera;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CameraControllerTest {
    @Test
    void stopRestoresTheSavedCameraState() {
        CameraSample initial = new CameraSample(new Vec3(1, 2, 3), Quaternion.IDENTITY);
        CameraPath path = new CameraPath("test", false, List.of(
                new CameraKeyframe(0, initial.position(), initial.orientation()),
                new CameraKeyframe(1, new Vec3(9, 9, 9), Quaternion.IDENTITY)));
        CameraController controller = new CameraController();
        controller.start(path, initial);
        controller.updateFromElapsedSeconds(0.5);
        assertEquals(initial, controller.stop());
    }
}
