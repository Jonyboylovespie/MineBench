package com.minebench.report;

import com.minebench.stats.RunAggregate;

import java.util.Locale;

public final class MarkdownReportWriter {
    private MarkdownReportWriter() {
    }

    public static String toMarkdown(BenchmarkReport report) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# MineBench report\n\n")
                .append("- Label: ").append(report.label).append('\n')
                .append("- Run ID: ").append(report.runId).append('\n')
                .append("- MineBench: ").append(report.minebenchVersion).append('\n')
                .append("- Suite: ").append(report.suiteVersion).append('\n')
                .append("- Mode: ").append(report.mode).append(" (settings: ").append(report.settingsMode).append(")\n")
                .append("- Workload target: ").append(report.workloadTarget).append(" (render scale: ")
                .append(Math.round(report.renderScale * 100.0)).append("%)\n")
                .append("- Single-run warning: ").append(report.singleRunWarning ? "yes" : "no").append("\n\n");

        markdown.append("## Environment\n\n")
                .append("- Minecraft: ").append(report.environment.minecraftVersion).append('\n')
                .append("- Renderer: ").append(report.environment.rendererIdentity).append('\n')
                .append("- Shaderpack: ").append(report.environment.shaderpackName).append('\n')
                .append("- World: ").append(report.environment.worldName).append(" (seed ")
                .append(report.environment.worldSeed).append(")\n")
                .append("- Display/framebuffer: ").append(report.environment.displayResolution)
                .append(" / ").append(report.environment.framebufferResolution).append('\n')
                .append("- Render distance/simulation distance: ").append(report.environment.renderDistance)
                .append(" / ").append(report.environment.simulationDistance).append("\n\n");

        markdown.append("## Scene results\n\n")
                .append("| Scene | Bottleneck | Median GPU use | Valid/total | Median average FPS | Median 1% low | Median p99 (ms) | CV |\n")
                .append("|---|---|---:|---:|---:|---:|---:|---:|\n");
        for (SceneReport scene : report.scenes) {
            RunAggregate aggregate = scene.aggregate;
            markdown.append('|').append(scene.sceneId).append('|')
                    .append(scene.bottleneck == null ? "unavailable" : scene.bottleneck.classification).append('|')
                    .append(scene.bottleneck == null ? "unavailable" : percentValue(scene.bottleneck.medianGpuUtilizationPercent)).append('|')
                    .append(aggregate.validIterations).append('/').append(aggregate.totalIterations).append('|')
                    .append(number(aggregate.medianAverageFps)).append('|')
                    .append(number(aggregate.medianOnePercentLowFps)).append('|')
                    .append(number(aggregate.medianP99FrameTimeMillis)).append('|')
                    .append(percent(aggregate.coefficientOfVariationAverageFps)).append("|\n");
        }
        markdown.append("\n## Iterations\n\n");
        for (SceneReport scene : report.scenes) {
            markdown.append("### ").append(scene.displayName).append("\n\n")
                    .append("Dimension/anchor: `").append(scene.dimension).append("` / `")
                    .append(scene.anchor).append("`; route hash: `").append(scene.routeHash)
                    .append("`; scene hash: `").append(scene.sceneContentHash).append("`.\n\n")
                    .append("| Iteration | Bottleneck | GPU use | Target met | Valid | Average FPS | 1% low FPS | p99 ms | Warnings |\n")
                    .append("|---:|---|---:|:---:|:---:|---:|---:|---:|---:|\n");
            for (IterationReport iteration : scene.iterations) {
                markdown.append('|').append(iteration.iteration).append('|')
                        .append(iteration.bottleneck == null ? "unavailable" : iteration.bottleneck.classification).append('|')
                        .append(iteration.bottleneck == null ? "unavailable" : percentValue(iteration.bottleneck.medianGpuUtilizationPercent)).append('|')
                        .append(iteration.bottleneck != null && iteration.bottleneck.targetConfirmed ? "yes" : "no").append('|')
                        .append(iteration.valid ? "yes" : "no").append('|')
                        .append(number(iteration.statistics.averageFps)).append('|')
                        .append(number(iteration.statistics.onePercentLowFps)).append('|')
                        .append(number(iteration.statistics.p99FrameTimeMillis)).append('|')
                        .append(iteration.warnings.size()).append("|\n");
            }
            markdown.append('\n');
        }

        markdown.append("## Validity warnings\n\n");
        if (report.warnings.isEmpty()) {
            markdown.append("No report-level warnings. See each iteration for iteration-level warnings.\n");
        } else {
            for (var warning : report.warnings) {
                markdown.append("- **").append(warning.severity()).append("** ")
                        .append(warning.code()).append(": ").append(warning.message()).append('\n');
            }
        }
        markdown.append("\n## Methodology\n\n")
                .append("Frame intervals are sampled at the selected world-frame boundary using `System.nanoTime()`. " )
                .append("The first timestamp is an anchor; setup, warm-up, sorting, report encoding, and file I/O occur outside measurement. " )
                .append("Percentiles use linear interpolation at `p * (n - 1)`, and 1% low FPS is the reciprocal of the mean frame time of the slowest 1% of accepted intervals.\n");
        return markdown.toString();
    }

    private static String number(double value) {
        return Double.isFinite(value) ? String.format(Locale.ROOT, "%.3f", value) : "unavailable";
    }

    private static String percent(double value) {
        return Double.isFinite(value) ? String.format(Locale.ROOT, "%.2f%%", value * 100.0) : "unavailable";
    }

    private static String percentValue(double value) {
        return Double.isFinite(value) ? String.format(Locale.ROOT, "%.1f%%", value) : "unavailable";
    }
}
