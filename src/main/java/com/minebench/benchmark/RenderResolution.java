package com.minebench.benchmark;

/** Pure render-scale calculation shared by the Minecraft adapter and unit tests. */
public record RenderResolution(int width, int height) {
    public static RenderResolution scaled(int nativeWidth, int nativeHeight, double scale, int maximumTextureSize) {
        int safeMaximum = Math.max(1, maximumTextureSize);
        return new RenderResolution(
                scaledDimension(nativeWidth, scale, safeMaximum),
                scaledDimension(nativeHeight, scale, safeMaximum));
    }

    private static int scaledDimension(int nativeDimension, double scale, int maximum) {
        int safeNative = Math.max(1, nativeDimension);
        double safeScale = Double.isFinite(scale) && scale > 0.0 ? scale : 1.0;
        return Math.max(1, Math.min(maximum, (int) Math.round(safeNative * safeScale)));
    }
}
