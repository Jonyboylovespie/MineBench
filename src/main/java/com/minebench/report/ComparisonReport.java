package com.minebench.report;

import com.minebench.environment.EnvironmentComparison;
import com.minebench.stats.ComparisonResult;

import java.util.List;

public final class ComparisonReport {
    public int schemaVersion = ReportSchema.CURRENT_VERSION;
    public String baselineLabel;
    public String candidateLabel;
    public EnvironmentComparison environment;
    public List<ComparisonResult> scenes;
    public boolean acknowledgedDifferences;
}
