package com.minebench.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Lightweight post-run SVG chart; raw CSV remains the source of truth. */
public final class ChartWriter {
    private ChartWriter() {
    }

    public static void writeFrameTimeChart(Path file, List<FrameSeries> series) throws IOException {
        long[] buckets = new long[5];
        if (series != null) {
            for (FrameSeries frameSeries : series) {
                if (frameSeries == null || frameSeries.samples() == null) continue;
                long[] values = frameSeries.samples().intervalsNanos();
                int count = Math.min(frameSeries.samples().storedCount(), values.length);
                for (int index = 0; index < count; index++) {
                    long value = values[index];
                    if (value <= 16_670_000L) buckets[0]++;
                    else if (value <= 33_330_000L) buckets[1]++;
                    else if (value <= 50_000_000L) buckets[2]++;
                    else if (value <= 100_000_000L) buckets[3]++;
                    else buckets[4]++;
                }
            }
        }
        long max = 1L;
        for (long bucket : buckets) max = Math.max(max, bucket);
        String[] labels = {"<=16.67ms", "16.67-33.33ms", "33.33-50ms", "50-100ms", ">100ms"};
        StringBuilder svg = new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"760\" height=\"280\" viewBox=\"0 0 760 280\">\n")
                .append("<rect width=\"100%\" height=\"100%\" fill=\"#101418\"/><text x=\"24\" y=\"28\" fill=\"#fff\" font-family=\"sans-serif\" font-size=\"18\">MineBench frame-time stutter histogram</text>\n");
        for (int index = 0; index < buckets.length; index++) {
            int x = 30 + index * 145;
            int height = (int) Math.round(190.0 * buckets[index] / max);
            int y = 230 - height;
            svg.append("<rect x=\"").append(x).append("\" y=\"").append(y)
                    .append("\" width=\"100\" height=\"").append(height).append("\" fill=\"#58a6ff\"/>\n")
                    .append("<text x=\"").append(x).append("\" y=\"250\" fill=\"#ddd\" font-family=\"sans-serif\" font-size=\"11\">")
                    .append(escape(labels[index])).append("</text>\n")
                    .append("<text x=\"").append(x).append("\" y=\"").append(Math.max(45, y - 6))
                    .append("\" fill=\"#fff\" font-family=\"sans-serif\" font-size=\"12\">")
                    .append(buckets[index]).append("</text>\n");
        }
        svg.append("</svg>\n");
        Files.writeString(file, svg, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
