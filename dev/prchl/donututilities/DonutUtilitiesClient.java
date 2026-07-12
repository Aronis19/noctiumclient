// Název souboru: DonutUtilitiesClient.java
package dev.prchl.donututilities;

import dev.prchl.donututilities.gui.ClickGuiScreen;
import dev.prchl.donututilities.config.ClientConfig;
import dev.prchl.donututilities.module.ModuleManager;
import dev.prchl.donututilities.render.ChunkOverlayRenderer;
import dev.prchl.donututilities.render.ModuleToastManager;
import dev.prchl.donututilities.scan.ScanManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.class_310;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DonutUtilitiesClient implements ClientModInitializer {
    public static final String MOD_ID = "donututilities";
    public static final String BUILD = "SELECTABLE-INFO-HUD";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ModuleManager MODULES = new ModuleManager();
    public static final ScanManager SCANNER = new ScanManager();

    private static final long CONFIG_SAVE_DELAY_MS = 750L;
    private static boolean rawMenuKeyWasDown;
    private static long lastTickWarningMs;
    private static boolean configReady;
    private static boolean configDirty;
    private static long configDirtyAt;

    @Override
    public void onInitializeClient() {
        MODULES.registerDefaults();
        ClientConfig.load(MODULES);
        configReady = true;
        MODULES.registerKeybinds();
        
        // Inicializace upraveného rendereru skrze statickou metodu register()
        ChunkOverlayRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(DonutUtilitiesClient::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            flushConfigOnShutdown();
            shutdownMelodify();
        });
        initializeMelodify();
        LOGGER.info("DonutSMP Utilities loaded");
    }

    private static void tick(class_310 client) {
        long window = client.method_22683().method_4490();
        boolean rawMenuKeyDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_INSERT) == GLFW.GLFW_PRESS;
        if (rawMenuKeyDown && !rawMenuKeyWasDown) {
            toggleMenu(client);
        }
        rawMenuKeyWasDown = rawMenuKeyDown;

        MODULES.tickKeybinds(client);
        MODULES.tick(client);
        ModuleToastManager.tick(MODULES);
        flushConfigIfNeeded();
        try {
            SCANNER.tick(client, MODULES);
        } catch (Throwable exception) {
            SCANNER.clear();
            long now = System.currentTimeMillis();
            if (now - lastTickWarningMs > 5000L) {
                LOGGER.warn("Skipped scanner tick to prevent a client crash", exception);
                lastTickWarningMs = now;
            }
        }
    }

    private static void toggleMenu(class_310 client) {
        if (client.field_1755 instanceof ClickGuiScreen) {
            client.method_1507(null);
        } else {
            client.method_1507(new ClickGuiScreen());
        }
    }

    public static void saveConfig() {
        configDirty = false;
        ClientConfig.save();
    }

    public static void markConfigDirty() {
        if (!configReady) {
            return;
        }
        configDirty = true;
        configDirtyAt = System.currentTimeMillis();
    }

    private static void flushConfigIfNeeded() {
        if (configDirty && System.currentTimeMillis() - configDirtyAt >= CONFIG_SAVE_DELAY_MS) {
            saveConfig();
        }
    }

    private static void flushConfigOnShutdown() {
        if (configDirty) {
            saveConfig();
        }
    }

    private static void initializeMelodify() {
        try {
            aqys.melodify.client.SpotifyApiClient.getInstance().initialize();
            ClientConfig.reapplySpotifyHud();
            LOGGER.info("Spotify integration initialized");
        } catch (Throwable exception) {
            LOGGER.warn("Spotify integration is unavailable", exception);
        }
    }

    private static void shutdownMelodify() {
        try {
            aqys.melodify.client.SpotifyApiClient.getInstance().shutdown();
        } catch (Throwable exception) {
            LOGGER.debug("Spotify integration shutdown skipped", exception);
        }
    }
}
