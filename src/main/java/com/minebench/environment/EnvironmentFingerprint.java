package com.minebench.environment;

import com.minebench.benchmark.SettingsSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Complete, sanitized environment record. Unknown values are explicit rather than silently omitted. */
public final class EnvironmentFingerprint {
    public String minebenchVersion = "unavailable";
    public String suiteVersion = "unavailable";
    public String minecraftVersion = "unavailable";
    public String fabricLoaderVersion = "unavailable";
    public String fabricApiVersion = "unavailable";
    public List<LoadedMod> loadedMods = new ArrayList<>();
    public String rendererIdentity = "unavailable";
    public String sodiumVersion = "unavailable";
    public boolean irisPresent;
    public String shaderpackName = "none";
    public String shaderpackHash = "unavailable";
    public List<String> resourcePacks = new ArrayList<>();
    public String displayResolution = "unavailable";
    public String framebufferResolution = "unavailable";
    public String guiScale = "unavailable";
    public String fullscreen = "unavailable";
    public String refreshRateHz = "unavailable";
    public String vsync = "unavailable";
    public String fpsLimit = "unavailable";
    public String renderDistance = "unavailable";
    public String simulationDistance = "unavailable";
    public String graphicsMode = "unavailable";
    public String clouds = "unavailable";
    public String particles = "unavailable";
    public String entityDistance = "unavailable";
    public String biomeBlend = "unavailable";
    public String mipmaps = "unavailable";
    public String anisotropicFiltering = "unavailable";
    public String osName = "unavailable";
    public String osArchitecture = "unavailable";
    public String javaVendor = "unavailable";
    public String javaVersion = "unavailable";
    public List<String> jvmArguments = new ArrayList<>();
    public String heapLimit = "unavailable";
    public String garbageCollector = "unavailable";
    public String gpuVendor = "unavailable";
    public String gpuRenderer = "unavailable";
    public String openGlVersion = "unavailable";
    public String driverString = "unavailable";
    public String cpuModel = "unavailable";
    public String logicalProcessors = "unavailable";
    public String worldSeed = "unavailable";
    public String worldName = "unavailable";
    public String dimension = "unavailable";
    public String sceneAnchor = "unavailable";
    public String sceneId = "unavailable";
    public String sceneContentHash = "unavailable";
    public String pathId = "unavailable";
    public String routeHash = "unavailable";
    public String durationSeconds = "unavailable";
    public String iterationNumber = "unavailable";
    public String capturedAt = "unavailable";
    public String monotonicTimingOrigin = "System.nanoTime";
    public Map<String, String> integrationMetadata = new LinkedHashMap<>();
    public SettingsSnapshot settingsSnapshot = new SettingsSnapshot();

    public static EnvironmentFingerprint unavailable() {
        return new EnvironmentFingerprint();
    }

    public EnvironmentFingerprint sanitizedCopy() {
        EnvironmentFingerprint copy = new EnvironmentFingerprint();
        copy.minebenchVersion = sanitize(minebenchVersion);
        copy.suiteVersion = sanitize(suiteVersion);
        copy.minecraftVersion = sanitize(minecraftVersion);
        copy.fabricLoaderVersion = sanitize(fabricLoaderVersion);
        copy.fabricApiVersion = sanitize(fabricApiVersion);
        copy.loadedMods = loadedMods == null ? new ArrayList<>() : loadedMods.stream()
                .map(mod -> new LoadedMod(sanitize(mod.id()), sanitize(mod.version())))
                .sorted(Comparator.comparing(LoadedMod::id).thenComparing(LoadedMod::version))
                .toList();
        copy.rendererIdentity = sanitize(rendererIdentity);
        copy.sodiumVersion = sanitize(sodiumVersion);
        copy.irisPresent = irisPresent;
        copy.shaderpackName = sanitize(shaderpackName);
        copy.shaderpackHash = sanitize(shaderpackHash);
        copy.resourcePacks = resourcePacks == null ? new ArrayList<>() : resourcePacks.stream()
                .map(EnvironmentFingerprint::sanitize).toList();
        copy.displayResolution = sanitize(displayResolution);
        copy.framebufferResolution = sanitize(framebufferResolution);
        copy.guiScale = sanitize(guiScale);
        copy.fullscreen = sanitize(fullscreen);
        copy.refreshRateHz = sanitize(refreshRateHz);
        copy.vsync = sanitize(vsync);
        copy.fpsLimit = sanitize(fpsLimit);
        copy.renderDistance = sanitize(renderDistance);
        copy.simulationDistance = sanitize(simulationDistance);
        copy.graphicsMode = sanitize(graphicsMode);
        copy.clouds = sanitize(clouds);
        copy.particles = sanitize(particles);
        copy.entityDistance = sanitize(entityDistance);
        copy.biomeBlend = sanitize(biomeBlend);
        copy.mipmaps = sanitize(mipmaps);
        copy.anisotropicFiltering = sanitize(anisotropicFiltering);
        copy.osName = sanitize(osName);
        copy.osArchitecture = sanitize(osArchitecture);
        copy.javaVendor = sanitize(javaVendor);
        copy.javaVersion = sanitize(javaVersion);
        copy.jvmArguments = jvmArguments == null ? new ArrayList<>() : jvmArguments.stream()
                .map(EnvironmentFingerprint::redactArgument).toList();
        copy.heapLimit = sanitize(heapLimit);
        copy.garbageCollector = sanitize(garbageCollector);
        copy.gpuVendor = sanitize(gpuVendor);
        copy.gpuRenderer = sanitize(gpuRenderer);
        copy.openGlVersion = sanitize(openGlVersion);
        copy.driverString = sanitize(driverString);
        copy.cpuModel = sanitize(cpuModel);
        copy.logicalProcessors = sanitize(logicalProcessors);
        copy.worldSeed = sanitize(worldSeed);
        copy.worldName = sanitize(worldName);
        copy.dimension = sanitize(dimension);
        copy.sceneAnchor = sanitize(sceneAnchor);
        copy.sceneId = sanitize(sceneId);
        copy.sceneContentHash = sanitize(sceneContentHash);
        copy.pathId = sanitize(pathId);
        copy.routeHash = sanitize(routeHash);
        copy.durationSeconds = sanitize(durationSeconds);
        copy.iterationNumber = sanitize(iterationNumber);
        copy.capturedAt = sanitize(capturedAt);
        copy.monotonicTimingOrigin = sanitize(monotonicTimingOrigin);
        if (integrationMetadata != null) {
            copy.integrationMetadata = new LinkedHashMap<>();
            integrationMetadata.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry ->
                    copy.integrationMetadata.put(sanitize(entry.getKey()), sanitize(entry.getValue())));
        }
        copy.settingsSnapshot = settingsSnapshot == null ? new SettingsSnapshot() : settingsSnapshot.copy();
        return copy;
    }

    private static String sanitize(String value) {
        return value == null || value.isBlank() ? "unavailable" : value;
    }

    private static String redactArgument(String value) {
        if (value == null) return "unavailable";
        return value.replaceAll("(?i)(/home/|/users/|[A-Za-z]:\\\\Users\\\\)[^ ]+", "<redacted-path>");
    }
}
