package com.minebench.camera;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CameraRouteResourceTest {
    @Test
    void packagedRoutesUseMinecraftResourceNamespaceLayout() throws Exception {
        for (String route : java.util.List.of("overworld_flythrough", "nether_flythrough", "end_flythrough")) {
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream(
                    "assets/minebench/camera/suite-1/" + route + ".json")) {
                assertNotNull(stream, route);
                CameraPath path = CameraPath.fromJson(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
                assertEquals(60.0, path.durationSeconds());
                assertTrue(path.loop());
                assertEquals(64, path.routeHash().length());
            }
        }
    }
}
