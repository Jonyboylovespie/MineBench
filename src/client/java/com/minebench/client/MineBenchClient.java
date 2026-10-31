package com.minebench.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.minebench.benchmark.ManagedPlanParser;
import com.minebench.benchmark.ManagedRunPlan;
import com.minebench.benchmark.BenchmarkPlan;
import com.minebench.benchmark.BenchmarkPreferences;
import com.minebench.benchmark.PreferencesStore;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.nio.file.Files;
import java.nio.file.Path;

public final class MineBenchClient implements ClientModInitializer {
    private static KeyMapping openKey;
    private static KeyMapping abortKey;
    private static ClientBenchmarkController controller;
    private static ManagedRunPlan pendingManagedPlan;
    private static BenchmarkPreferences preferences;
    private static boolean managedWorldRequested;

    public static ClientBenchmarkController controller() {
        return controller;
    }

    public static BenchmarkPlan applyPreferences(BenchmarkPlan plan) {
        if (plan != null && preferences != null) {
            plan.withWorkloadTarget(com.minebench.benchmark.WorkloadTarget.parse(preferences.defaultWorkloadTarget));
        }
        return plan;
    }

    public static com.minebench.benchmark.WorkloadTarget workloadTarget() {
        return com.minebench.benchmark.WorkloadTarget.parse(
                preferences == null ? "balanced" : preferences.defaultWorkloadTarget);
    }

    public static void setWorkloadTarget(com.minebench.benchmark.WorkloadTarget target) {
        if (preferences == null) preferences = PreferencesStore.defaults();
        preferences.defaultWorkloadTarget = (target == null
                ? com.minebench.benchmark.WorkloadTarget.BALANCED : target).id();
        try {
            PreferencesStore.save(FabricLoader.getInstance().getConfigDir().resolve("minebench/config.json"), preferences);
        } catch (Exception exception) {
            System.err.println("[MineBench] Could not save workload target: " + exception.getMessage());
        }
    }

    @Override
    public void onInitializeClient() {
        preferences = loadPreferences();
        controller = new ClientBenchmarkController(preferences.frameCapacity);
        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.minebench.open", InputConstants.Type.KEYBOARD, InputConstants.KEY_F8, KeyMapping.Category.MISC));
        abortKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.minebench.abort", InputConstants.Type.KEYBOARD, InputConstants.KEY_ESCAPE, KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(MineBenchClient::onClientTick);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommands.literal("minebench")
                        .then(ClientCommands.literal("quick").executes(context -> {
                            controller.startPlan(Minecraft.getInstance(), applyPreferences(BenchmarkPlan.quick()));
                            return 1;
                        }))
                        .then(ClientCommands.literal("standard").executes(context -> {
                            controller.startPlan(Minecraft.getInstance(), applyPreferences(BenchmarkPlan.standard("Command run")));
                            return 1;
                        }))
                        .then(ClientCommands.literal("extended").executes(context -> {
                            controller.startPlan(Minecraft.getInstance(), applyPreferences(BenchmarkPlan.extended("Command suite")));
                            return 1;
                        }))
                        .then(ClientCommands.literal("abort").executes(context -> {
                            controller.abort(Minecraft.getInstance());
                            return 1;
                        }))
        ));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (controller != null && controller.active()) controller.abort(client);
        });
        tryManagedAutoStart();
    }

    private static BenchmarkPreferences loadPreferences() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("minebench/config.json");
        try {
            BenchmarkPreferences loaded = PreferencesStore.load(file);
            if (!Files.isRegularFile(file)) PreferencesStore.save(file, loaded);
            return loaded;
        } catch (Exception exception) {
            System.err.println("[MineBench] Could not load preferences: " + exception.getMessage());
            return PreferencesStore.defaults();
        }
    }

    private static void onClientTick(Minecraft client) {
        if (pendingManagedPlan != null && client.level == null && pendingManagedPlan.autoCreateWorld
                && !managedWorldRequested && client.gui.screen() != null) {
            managedWorldRequested = true;
            BenchmarkWorldManager.createOrOpen(client, pendingManagedPlan.toBenchmarkPlan(), client.gui.screen());
        }
        if (pendingManagedPlan != null && client.level != null && client.player != null && !controller.active()) {
            ManagedRunPlan plan = pendingManagedPlan;
            pendingManagedPlan = null;
            managedWorldRequested = false;
            controller.startManaged(client, plan);
        }
        while (openKey.consumeClick()) {
            client.gui.setScreen(new MineBenchScreen(client.gui.screen()));
        }
        while (abortKey.consumeClick()) {
            if (controller != null && controller.active()) controller.abort(client);
        }
        if (controller != null) controller.tick(client, System.nanoTime());
    }

    /** Called by the version-pinned Minecraft.renderFrame hook after one client frame completes. */
    public static void onRenderedFrame() {
        if (controller == null) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.gui.screen() == null) {
            controller.core().onPresentedFrame(System.nanoTime(), 0, client.getGpuUtilization());
        }
    }

    private static void tryManagedAutoStart() {
        Path planPath = FabricLoader.getInstance().getGameDir().resolve("config/minebench/managed-plan.json");
        boolean requested = Boolean.getBoolean("minebench.managed") || Files.exists(planPath);
        if (!requested) return;
        // Auto-start is deferred until a world is available; this is checked on every client tick by the user UI.
        if (Files.isRegularFile(planPath)) {
            try {
                ManagedRunPlan plan = ManagedPlanParser.read(planPath);
                if (plan.autoStart || Boolean.getBoolean("minebench.managed")) {
                    pendingManagedPlan = plan;
                }
            } catch (Exception exception) {
                System.err.println("[MineBench] Managed plan could not be loaded: " + exception.getMessage());
            }
        }
    }
}
