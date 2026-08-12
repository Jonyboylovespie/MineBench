package com.minebench.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvFrameWriter {
    private CsvFrameWriter() {
    }

    public static void write(Path file, List<FrameSeries> series) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("iteration,scene,frame_index,elapsed_nanoseconds,frame_interval_nanoseconds,gpu_nanoseconds,gpu_utilization_percent,event_flags");
            writer.newLine();
            if (series == null) {
                return;
            }
            for (FrameSeries frameSeries : series) {
                if (frameSeries == null || frameSeries.samples() == null) continue;
                var samples = frameSeries.samples();
                long elapsed = 0L;
                long[] intervals = samples.intervalsNanos();
                long[] gpu = samples.gpuNanos();
                double[] gpuUtilization = samples.gpuUtilizationPercent();
                int[] flags = samples.eventFlags();
                int count = Math.min(samples.storedCount(), intervals.length);
                for (int index = 0; index < count; index++) {
                    elapsed += intervals[index];
                    writer.write(Integer.toString(frameSeries.iteration()));
                    writer.write(',');
                    writer.write(csv(frameSeries.sceneId()));
                    writer.write(',');
                    writer.write(Integer.toString(index));
                    writer.write(',');
                    writer.write(Long.toString(elapsed));
                    writer.write(',');
                    writer.write(Long.toString(intervals[index]));
                    writer.write(',');
                    if (gpu != null && index < gpu.length && gpu[index] > 0L) {
                        writer.write(Long.toString(gpu[index]));
                    }
                    writer.write(',');
                    if (gpuUtilization != null && index < gpuUtilization.length
                            && Double.isFinite(gpuUtilization[index])) {
                        writer.write(Double.toString(gpuUtilization[index]));
                    }
                    writer.write(',');
                    writer.write(Integer.toString(index < flags.length ? flags[index] : 0));
                    writer.newLine();
                }
            }
        }
    }

    private static String csv(String value) {
        if (value == null) return "";
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0 && value.indexOf('\n') < 0) return value;
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
