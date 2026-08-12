package com.minebench.benchmark;

import java.util.ArrayList;
import java.util.List;

/** Records contamination evidence without silently deleting the affected iteration. */
public final class BenchmarkValidity {
    private final List<ValidityWarning> warnings = new ArrayList<>();
    private boolean valid = true;

    public void info(ValidityCode code, String message, long now) {
        add(code, ValiditySeverity.INFO, message, now, false);
    }

    public void warn(ValidityCode code, String message, long now) {
        add(code, ValiditySeverity.WARNING, message, now, false);
    }

    public void invalidate(ValidityCode code, String message, long now) {
        add(code, ValiditySeverity.ERROR, message, now, true);
    }

    public boolean isValid() {
        return valid;
    }

    public List<ValidityWarning> warnings() {
        return List.copyOf(warnings);
    }

    public void merge(BenchmarkValidity other) {
        if (other == null) {
            return;
        }
        warnings.addAll(other.warnings);
        valid &= other.valid;
    }

    private void add(ValidityCode code, ValiditySeverity severity, String message, long now, boolean invalidates) {
        warnings.add(new ValidityWarning(code, severity, message, now, invalidates));
        if (invalidates) {
            valid = false;
        }
    }
}
