package com.minebench.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SceneManifestValidator {
    private SceneManifestValidator() {
    }

    public static SceneManifestValidation validate(SceneManifest expected, SceneManifest actual) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        if (expected == null || actual == null) {
            return new SceneManifestValidation(false, List.of("Expected and actual manifests are required"), warnings);
        }
        compare(errors, "sceneId", expected.sceneId, actual.sceneId);
        compare(errors, "dimension", expected.dimension, actual.dimension);
        compare(errors, "contentHash", expected.contentHash, actual.contentHash);
        compare(errors, "routeHash", expected.routeHash, actual.routeHash);
        if (expected.seed != actual.seed) {
            errors.add("seed differs: expected " + expected.seed + ", actual " + actual.seed);
        }
        compare(errors, "cameraResource", expected.cameraResource, actual.cameraResource);
        if (Double.compare(expected.anchorX, actual.anchorX) != 0
                || Double.compare(expected.anchorY, actual.anchorY) != 0
                || Double.compare(expected.anchorZ, actual.anchorZ) != 0) errors.add("anchor differs");
        if (expected.readinessRadiusChunks != actual.readinessRadiusChunks) errors.add("readiness radius differs");
        if (expected.geometryRebuild != actual.geometryRebuild) errors.add("geometry rebuild flag differs");
        if ("unavailable".equals(actual.contentHash)) {
            warnings.add("Actual scene content hash is unavailable");
        }
        return new SceneManifestValidation(errors.isEmpty(), errors, warnings);
    }

    private static void compare(List<String> errors, String field, String expected, String actual) {
        if (!Objects.equals(expected, actual)) {
            errors.add(field + " differs: expected " + expected + ", actual " + actual);
        }
    }
}
