package com.minebench.scene;

import java.util.List;

public record SceneManifestValidation(boolean valid, List<String> errors, List<String> warnings) {
    public SceneManifestValidation {
        errors = List.copyOf(errors == null ? List.of() : errors);
        warnings = List.copyOf(warnings == null ? List.of() : warnings);
    }
}
