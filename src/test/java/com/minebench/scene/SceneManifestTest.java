package com.minebench.scene;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SceneManifestTest {
    @Test
    void generatedBlueprintIsSelfConsistent() {
        SceneManifest manifest = SceneGenerator.blueprint(SceneDefinitions.find("overworld_panorama").orElseThrow(),
                SceneGenerator.SUITE_SEED, "none");
        assertEquals(manifest.contentHash, SceneContentHasher.sha256(manifest));
        assertEquals("minecraft:overworld", manifest.dimension);
        assertEquals(2048.5, manifest.anchorX);
        SceneManifestValidation validation = SceneManifestValidator.validate(manifest, manifest.copy());
        assertTrue(validation.valid());
    }
}
