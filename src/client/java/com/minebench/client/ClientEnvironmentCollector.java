package com.minebench.client;

import com.minebench.MineBench;
import com.minebench.api.MineBenchApi;
import com.minebench.benchmark.SettingsSnapshot;
import com.minebench.environment.EnvironmentFingerprint;
import com.minebench.environment.LoadedMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HexFormat;
import java.util.Properties;

/** Collects environment metadata once per run, never from the frame callback. */
public final class ClientEnvironmentCollector {
    private ClientEnvironmentCollector() {
    }

    public static EnvironmentFingerprint capture(Minecraft client, String sceneId, int durationSeconds, int iteration) {
        FabricLoader loader = FabricLoader.getInstance();
        EnvironmentFingerprint fingerprint = EnvironmentFingerprint.unavailable();
        fingerprint.minebenchVersion = MineBench.MOD_VERSION;
        fingerprint.suiteVersion = MineBench.SUITE_VERSION;
        fingerprint.minecraftVersion = modVersion(loader, "minecraft");
        fingerprint.fabricLoaderVersion = modVersion(loader, "fabricloader");
        fingerprint.fabricApiVersion = modVersion(loader, "fabric-api");
        fingerprint.loadedMods = loader.getAllMods().stream()
                .map(container -> new LoadedMod(container.getMetadata().getId(),
                        container.getMetadata().getVersion().getFriendlyString()))
                .sorted(Comparator.comparing(LoadedMod::id).thenComparing(LoadedMod::version))
                .toList();
        boolean sodium = loader.isModLoaded("sodium");
        fingerprint.rendererIdentity = sodium ? "Sodium" : "standard renderer";
        fingerprint.sodiumVersion = sodium ? modVersion(loader, "sodium") : "not installed";
        fingerprint.irisPresent = loader.isModLoaded("iris");
        captureShaderpack(loader.getGameDir(), fingerprint);
        fingerprint.resourcePacks = resourcePackNames(client);
        fingerprint.displayResolution = windowValue(client, "getWidth", "unavailable") + "x"
                + windowValue(client, "getHeight", "unavailable");
        fingerprint.framebufferResolution = fingerprint.displayResolution;
        fingerprint.fullscreen = optionValue(client, "fullscreen", "unavailable");
        fingerprint.refreshRateHz = windowValue(client, "getRefreshRate", "unavailable");
        fingerprint.guiScale = optionValue(client, "guiScale", "unavailable");
        fingerprint.vsync = optionValue(client, "enableVsync", "unavailable");
        fingerprint.fpsLimit = optionValue(client, "framerateLimit", "unavailable");
        fingerprint.renderDistance = optionValue(client, "renderDistance", "unavailable");
        fingerprint.simulationDistance = optionValue(client, "simulationDistance", "unavailable");
        fingerprint.graphicsMode = optionValue(client, "graphicsMode", "unavailable");
        fingerprint.clouds = optionValue(client, "renderClouds", "unavailable");
        fingerprint.particles = optionValue(client, "particles", "unavailable");
        fingerprint.entityDistance = optionValue(client, "entityDistanceScaling", "unavailable");
        fingerprint.biomeBlend = optionValue(client, "biomeBlendRadius", "unavailable");
        fingerprint.mipmaps = optionValue(client, "mipmapLevels", "unavailable");
        fingerprint.anisotropicFiltering = "unavailable";
        fingerprint.osName = System.getProperty("os.name", "unavailable");
        fingerprint.osArchitecture = System.getProperty("os.arch", "unavailable");
        fingerprint.javaVendor = System.getProperty("java.vendor", "unavailable");
        fingerprint.javaVersion = System.getProperty("java.version", "unavailable");
        fingerprint.jvmArguments = new ArrayList<>(ManagementFactory.getRuntimeMXBean().getInputArguments());
        fingerprint.heapLimit = Long.toString(maxHeapBytes());
        fingerprint.garbageCollector = ManagementFactory.getGarbageCollectorMXBeans().stream()
                .map(bean -> bean.getName()).sorted().reduce((a, b) -> a + ", " + b).orElse("unavailable");
        fingerprint.gpuVendor = glString(org.lwjgl.opengl.GL11.GL_VENDOR);
        fingerprint.gpuRenderer = glString(org.lwjgl.opengl.GL11.GL_RENDERER);
        fingerprint.openGlVersion = glString(org.lwjgl.opengl.GL11.GL_VERSION);
        fingerprint.driverString = fingerprint.openGlVersion;
        fingerprint.cpuModel = cpuModel();
        fingerprint.logicalProcessors = Integer.toString(Runtime.getRuntime().availableProcessors());
        fingerprint.sceneId = sceneId == null ? "unavailable" : sceneId;
        fingerprint.durationSeconds = Integer.toString(durationSeconds);
        fingerprint.iterationNumber = Integer.toString(iteration);
        fingerprint.integrationMetadata = MineBenchApi.collectMetadata();
        fingerprint.settingsSnapshot = settingsSnapshot(client);
        if (client.getSingleplayerServer() != null) {
            fingerprint.worldSeed = Long.toString(client.getSingleplayerServer().getWorldGenSettings().options().seed());
            fingerprint.worldName = client.getSingleplayerServer().getWorldData().getLevelName();
        }
        if (client.level != null) fingerprint.dimension = client.level.dimension().identifier().toString();
        if (client.player != null) fingerprint.sceneAnchor = String.format(Locale.ROOT, "%.1f, %.1f, %.1f",
                client.player.getX(), client.player.getEyeY(), client.player.getZ());
        return fingerprint.sanitizedCopy();
    }

    private static String modVersion(FabricLoader loader, String id) {
        return loader.getModContainer(id)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unavailable");
    }

    private static List<String> resourcePackNames(Minecraft client) {
        Object repository = invoke(client, "getResourcePackRepository");
        Object selected = repository == null ? null : invoke(repository, "getSelectedPacks");
        if (selected instanceof Iterable<?> iterable) {
            List<String> names = new ArrayList<>();
            for (Object pack : iterable) {
                Object id = invoke(pack, "getId");
                Object name = id == null ? invoke(pack, "getName") : id;
                names.add(sanitizePackName(name == null ? String.valueOf(pack) : String.valueOf(name)));
            }
            return names.stream().sorted().toList();
        }
        return List.of("unavailable");
    }

    private static SettingsSnapshot settingsSnapshot(Minecraft client) {
        SettingsSnapshot snapshot = new SettingsSnapshot();
        snapshot.capturedAt = java.time.Instant.now().toString();
        snapshot.mode = "record-only";
        String[] keys = {"fullscreen", "enableVsync", "framerateLimit", "inactivityFpsLimit", "renderDistance", "simulationDistance",
                "graphicsMode", "renderClouds", "particles", "entityDistanceScaling", "biomeBlendRadius", "mipmapLevels"};
        for (String key : keys) snapshot.values.put(key, optionValue(client, key, "unavailable"));
        return snapshot;
    }

    private static String optionValue(Minecraft client, String method, String fallback) {
        Object options = field(client, "options");
        Object value = options == null ? null : invoke(options, method);
        if (value == null && options != null) value = field(options, method);
        Object optionValue = value == null ? null : invoke(value, "get");
        if (optionValue == null && value != null) optionValue = invoke(value, "getValue");
        if (optionValue != null) value = optionValue;
        return value == null ? fallback : String.valueOf(value);
    }

    private static String sanitizePackName(String name) {
        return name.replaceAll("(?i)(/home/|/users/|[A-Za-z]:\\\\Users\\\\)[^ ]+", "<redacted-path>");
    }

    private static String windowValue(Minecraft client, String method, String fallback) {
        Object window = invoke(client, "getWindow");
        Object value = window == null ? null : invoke(window, method);
        return value == null ? fallback : String.valueOf(value);
    }

    public static String windowSignature(Minecraft client) {
        return windowValue(client, "getWidth", "unavailable") + "x"
                + windowValue(client, "getHeight", "unavailable") + ":"
                + optionValue(client, "fullscreen", "unavailable");
    }

    public static String settingsSignature(Minecraft client) {
        String[] keys = {"enableVsync", "framerateLimit", "inactivityFpsLimit", "renderDistance", "simulationDistance", "graphicsMode",
                "renderClouds", "particles", "entityDistanceScaling", "biomeBlendRadius", "mipmapLevels"};
        StringBuilder signature = new StringBuilder();
        for (String key : keys) {
            signature.append(key).append('=').append(optionValue(client, key, "unavailable")).append(';');
        }
        return signature.toString();
    }

    public static String worldSignature(Minecraft client) {
        if (client.level == null) return "no-world";
        Object dimension = invoke(client.level, "dimension");
        return dimension == null ? client.level.getClass().getName() : String.valueOf(dimension);
    }

    public static Boolean windowFocused(Minecraft client) {
        Object window = invoke(client, "getWindow");
        Object focused = window == null ? null : invoke(window, "isFocused");
        if (focused instanceof Boolean value) return value;
        Object active = invoke(client, "isWindowActive");
        return active instanceof Boolean value ? value : null;
    }

    private static long maxHeapBytes() {
        long maximum = 0L;
        for (MemoryPoolMXBean bean : ManagementFactory.getMemoryPoolMXBeans()) {
            if (bean.getUsage() != null && bean.getUsage().getMax() > maximum) maximum = bean.getUsage().getMax();
        }
        return maximum > 0L ? maximum : Runtime.getRuntime().maxMemory();
    }

    private static String glString(int name) {
        try {
            String value = org.lwjgl.opengl.GL11.glGetString(name);
            return value == null || value.isBlank() ? "unavailable" : value;
        } catch (RuntimeException | LinkageError ignored) {
            return "unavailable";
        }
    }

    private static String cpuModel() {
        String fromEnvironment = System.getenv("PROCESSOR_IDENTIFIER");
        if (fromEnvironment != null && !fromEnvironment.isBlank()) return fromEnvironment;
        Path cpuInfo = Path.of("/proc/cpuinfo");
        if (Files.isRegularFile(cpuInfo)) {
            try {
                return Files.lines(cpuInfo).filter(line -> line.toLowerCase(Locale.ROOT).startsWith("model name"))
                        .map(line -> line.substring(line.indexOf(':') + 1).trim()).findFirst().orElse("unavailable");
            } catch (IOException ignored) {
                return "unavailable";
            }
        }
        return "unavailable";
    }

    private static void captureShaderpack(Path gameDir, EnvironmentFingerprint fingerprint) {
        if (!fingerprint.irisPresent) {
            fingerprint.shaderpackName = "none (Iris not installed)";
            fingerprint.shaderpackHash = "none";
            return;
        }
        Properties properties = new Properties();
        Path irisConfig = gameDir.resolve("config").resolve("iris.properties");
        try (InputStream input = Files.newInputStream(irisConfig)) {
            properties.load(input);
        } catch (IOException ignored) {
            fingerprint.shaderpackName = "unavailable";
            return;
        }
        boolean enabled = Boolean.parseBoolean(properties.getProperty("enableShaders", "false"));
        String selected = properties.getProperty("shaderPack", "").trim();
        if (!enabled || selected.isBlank()) {
            fingerprint.shaderpackName = "none (disabled)";
            fingerprint.shaderpackHash = "none";
            return;
        }
        fingerprint.shaderpackName = Path.of(selected).getFileName().toString();
        Path pack = gameDir.resolve("shaderpacks").resolve(selected).normalize();
        if (!pack.startsWith(gameDir.resolve("shaderpacks").normalize()) || !Files.isRegularFile(pack)) return;
        try {
            fingerprint.shaderpackHash = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(pack)));
        } catch (Exception ignored) {
            fingerprint.shaderpackHash = "unavailable";
        }
    }

    private static Object field(Object target, String name) {
        if (target == null) return null;
        try {
            java.lang.reflect.Field field = target.getClass().getField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static Object invoke(Object target, String name) {
        if (target == null) return null;
        try {
            java.lang.reflect.Method method = target.getClass().getMethod(name);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}
