package com.minebench.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Compares only fields that affect rendering results; timestamps and iteration labels are intentionally ignored. */
public final class EnvironmentComparator {
    private EnvironmentComparator() {
    }

    public static EnvironmentComparison compare(EnvironmentFingerprint left, EnvironmentFingerprint right) {
        List<EnvironmentDifference> differences = new ArrayList<>();
        if (left == null || right == null) {
            differences.add(new EnvironmentDifference("environment", String.valueOf(left), String.valueOf(right), true));
            return new EnvironmentComparison(false, differences);
        }
        compare(differences, "minecraftVersion", left.minecraftVersion, right.minecraftVersion, true);
        compare(differences, "fabricLoaderVersion", left.fabricLoaderVersion, right.fabricLoaderVersion, true);
        compare(differences, "fabricApiVersion", left.fabricApiVersion, right.fabricApiVersion, true);
        compare(differences, "loadedMods", left.loadedMods, right.loadedMods, true);
        compare(differences, "rendererIdentity", left.rendererIdentity, right.rendererIdentity, true);
        compare(differences, "sodiumVersion", left.sodiumVersion, right.sodiumVersion, true);
        compare(differences, "irisPresent", left.irisPresent, right.irisPresent, true);
        compare(differences, "shaderpackName", left.shaderpackName, right.shaderpackName, true);
        compare(differences, "resourcePacks", left.resourcePacks, right.resourcePacks, true);
        compare(differences, "displayResolution", left.displayResolution, right.displayResolution, true);
        compare(differences, "framebufferResolution", left.framebufferResolution, right.framebufferResolution, true);
        compare(differences, "fullscreen", left.fullscreen, right.fullscreen, true);
        compare(differences, "refreshRateHz", left.refreshRateHz, right.refreshRateHz, true);
        compare(differences, "vsync", left.vsync, right.vsync, true);
        compare(differences, "fpsLimit", left.fpsLimit, right.fpsLimit, true);
        compare(differences, "renderDistance", left.renderDistance, right.renderDistance, true);
        compare(differences, "simulationDistance", left.simulationDistance, right.simulationDistance, true);
        compare(differences, "graphicsMode", left.graphicsMode, right.graphicsMode, true);
        compare(differences, "clouds", left.clouds, right.clouds, false);
        compare(differences, "particles", left.particles, right.particles, true);
        compare(differences, "entityDistance", left.entityDistance, right.entityDistance, true);
        compare(differences, "biomeBlend", left.biomeBlend, right.biomeBlend, true);
        compare(differences, "mipmaps", left.mipmaps, right.mipmaps, false);
        compare(differences, "anisotropicFiltering", left.anisotropicFiltering, right.anisotropicFiltering, true);
        compare(differences, "osName", left.osName, right.osName, true);
        compare(differences, "osArchitecture", left.osArchitecture, right.osArchitecture, true);
        compare(differences, "javaVersion", left.javaVersion, right.javaVersion, true);
        compare(differences, "jvmArguments", left.jvmArguments, right.jvmArguments, true);
        compare(differences, "heapLimit", left.heapLimit, right.heapLimit, true);
        compare(differences, "gpuVendor", left.gpuVendor, right.gpuVendor, true);
        compare(differences, "gpuRenderer", left.gpuRenderer, right.gpuRenderer, true);
        compare(differences, "openGlVersion", left.openGlVersion, right.openGlVersion, true);
        compare(differences, "driverString", left.driverString, right.driverString, true);
        compare(differences, "logicalProcessors", left.logicalProcessors, right.logicalProcessors, true);
        compare(differences, "worldSeed", left.worldSeed, right.worldSeed, true);
        compare(differences, "sceneId", left.sceneId, right.sceneId, true);
        compare(differences, "sceneContentHash", left.sceneContentHash, right.sceneContentHash, true);
        compare(differences, "routeHash", left.routeHash, right.routeHash, true);
        return new EnvironmentComparison(differences.stream().noneMatch(EnvironmentDifference::blocksComparison), differences);
    }

    private static void compare(List<EnvironmentDifference> differences, String field, Object left, Object right,
                                boolean blocksComparison) {
        if (!Objects.equals(left, right)) {
            differences.add(new EnvironmentDifference(field, String.valueOf(left), String.valueOf(right), blocksComparison));
        }
    }
}
