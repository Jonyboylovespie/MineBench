package com.minebench.camera;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CameraPathTest {
    @Test
    void endpointsAndLoopingAreDeterministic() {
        CameraPath path = new CameraPath("test", true, List.of(
                new CameraKeyframe(0.0, new Vec3(0, 0, 0), Quaternion.IDENTITY),
                new CameraKeyframe(1.0, new Vec3(10, 0, 0), Quaternion.IDENTITY),
                new CameraKeyframe(2.0, new Vec3(20, 0, 0), Quaternion.IDENTITY)
        ));
        assertEquals(path.sampleAtSeconds(0.0), path.sampleAtSeconds(2.0));
        assertEquals(path.sampleNormalized(0.0), path.sampleAtSeconds(0.0));
        assertEquals(path.sampleNormalized(0.5), path.sampleAtSeconds(1.0));
        assertEquals(path.routeHash(), new CameraPath("test", true, path.keyframes()).routeHash());
        assertNotEquals(path.routeHash(), new CameraPath("other", true, path.keyframes()).routeHash());
        assertEquals(path.routeHash(), CameraPath.fromJson(path.toJson()).routeHash());
    }
}
