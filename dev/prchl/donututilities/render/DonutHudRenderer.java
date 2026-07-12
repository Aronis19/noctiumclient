package dev.prchl.donututilities.render;

import dev.prchl.donututilities.DonutUtilitiesClient;
import dev.prchl.donututilities.module.HudSettings;
import dev.prchl.donututilities.module.Module;
import dev.prchl.donututilities.render.shape.RoundedRectRenderer;
import dev.prchl.donututilities.scan.BaseMarker;
import dev.prchl.donututilities.scan.MarkerType;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_9779;

public final class DonutHudRenderer {
    private static final long MODULE_LIST_ENTER_MS = 230L;
    private static final long MODULE_LIST_EXIT_MS = 260L;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Map<String, ModuleListState> moduleListStates = new HashMap<>();
    private static boolean moduleListInitialized;

    private DonutHudRenderer() {
    }

    public static void render(class_332 graphics, class_9779 tickCounter) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null || client.field_1690.field_1842) {
            return;
        }

        ModuleToastManager.render(graphics);
        drawWatermark(graphics);
        if (DonutUtilitiesClient.MODULES.enabled("module_list")) {
            drawModuleList(graphics);
        }
        if (!DonutUtilitiesClient.MODULES.enabled("hud")) {
            return;
        }

        drawInfoStack(graphics, client);

        boolean showSpotifyHud = DonutUtilitiesClient.MODULES.enabled("radio")
                && DonutUtilitiesClient.MODULES.hudSettings().spotifyHud();
        if (showSpotifyHud) {
            try {
                aqys.melodify.client.HUDSettings settings = aqys.melodify.client.HUDSettings.getInstance();
                settings.setAutoHideInMenus(false);
                settings.setLyricsEnabled(false);
                aqys.melodify.client.HUDRenderer.onHudRender(graphics, tickCounter);
            } catch (Throwable ignored) {
                // The bundled Spotify HUD is optional at runtime.
            }
        }

        if (client.field_1755 != null) {
            return;
        }

        int x = Math.max(14, client.method_22683().method_4486() - 246);
        int y = 14;
        int width = 232;
        int gap = 8;

        class_2338 pos = client.field_1724.method_24515();
        int nextY = y;
        if (DonutUtilitiesClient.MODULES.hudSettings().baseScan()) {
            drawScanCard(graphics, x, nextY, width, 70, pos);
            nextY += 70 + gap;
        }
        if (DonutUtilitiesClient.MODULES.hudSettings().espStats()) {
            drawEspCard(graphics, x, nextY, width, 78);
            nextY += 78 + gap;
        }
    }

    private static void drawCard(class_332 graphics, int x, int y, int width, int height, String title, String icon) {
        HudTheme.frame(graphics, x, y, width, height);
        graphics.method_25294(x + 10, y + 27, x + width - 10, y + 28, 0x553D4655);
        HudTheme.text(graphics, title, x + 12, y + 8, HudTheme.TEXT);
        drawSmallIcon(graphics, x + width - 24, y + 7, icon);
    }

    private static void drawScanCard(class_332 graphics, int x, int y, int width, int height, class_2338 pos) {
        drawCard(graphics, x, y, width, height, "DONUT SMP", "compass");
        HudTheme.text(graphics, "POSITION", x + 12, y + 36, HudTheme.MUTED);
        String xyz = pos.method_10263() + "  " + pos.method_10264() + "  " + pos.method_10260();
        HudTheme.text(graphics, HudTheme.fit(xyz, HudTheme.unscaledWidth(width - 24)), x + 12, y + 48, HudTheme.BLUE);
        HudTheme.text(graphics, "WORLD READY", x + 12, y + 60, 0xFF8FE0A0);
        HudTheme.text(graphics, "LIVE", x + width - 42, y + 60, HudTheme.MUTED);
    }

    private static void drawEspCard(class_332 graphics, int x, int y, int width, int height) {
        drawCard(graphics, x, y, width, height, "SCAN OVERVIEW", "scan");
        int markers = DonutUtilitiesClient.SCANNER.markers().size();
        long storage = DonutUtilitiesClient.SCANNER.markers().stream().filter(marker -> marker.type() == MarkerType.STORAGE).count();
        long chunks = DonutUtilitiesClient.SCANNER.markers().stream().filter(marker -> marker.type() == MarkerType.SUS_CHUNK).count();
        long blocks = DonutUtilitiesClient.SCANNER.markers().stream().filter(marker -> marker.type() == MarkerType.BLOCK_ESP).count();
        long suspicious = DonutUtilitiesClient.SCANNER.markers().stream().filter(marker -> marker.type() == MarkerType.SUSPICIOUS).count();
        drawStat(graphics, x + 12, y + 36, "MARKERS", markers, markers > 0 ? HudTheme.BLUE : HudTheme.MUTED);
        drawStat(graphics, x + 122, y + 36, "STORAGE", storage, storage > 0 ? 0xFFFFD25A : HudTheme.MUTED);
        drawStat(graphics, x + 12, y + 52, "CHUNKS", chunks, chunks > 0 ? 0xFFFF7A90 : HudTheme.MUTED);
        drawStat(graphics, x + 122, y + 52, "BLOCKS", blocks, blocks > 0 ? 0xFF9FE870 : HudTheme.MUTED);
        BaseMarker strongest = DonutUtilitiesClient.SCANNER.strongestMarkers(1).stream().findFirst().orElse(null);
        String signal = strongest == null ? "NO BASE SIGNAL" : HudTheme.fit(strongest.label().toUpperCase(java.util.Locale.ROOT), HudTheme.unscaledWidth(width - 24));
        HudTheme.text(graphics, signal, x + 12, y + 67, suspicious > 0 ? 0xFFFF9B7A : HudTheme.MUTED);
    }

    private static void drawStat(class_332 graphics, int x, int y, String label, long value, int color) {
        HudTheme.text(graphics, label, x, y, HudTheme.MUTED);
        HudTheme.text(graphics, Long.toString(value), x + 56, y, color);
    }

    private static void drawWatermark(class_332 graphics) {
        String text = "Noctium Client+";
        int x = 8;
        int y = 8;
        int width = HudTheme.scaledWidth(text) + 18;
        int height = 16;
        if (!NanoVgWatermarkRenderer.drawBackground(graphics, x, y, width, height, 0x8D151820)) {
            graphics.method_25294(x, y, x + width, y + height, 0x8D151820);
        }
        graphics.method_25294(x, y, x + 2, y + height, GuiTheme.ACCENT);
        int textX = x + 8 + Math.max(0, (width - 8 - HudTheme.scaledWidth(text)) / 2);
        int textY = y + Math.max(0, (height - Math.round(9 * HudTheme.TEXT_SCALE)) / 2);
        HudTheme.text(graphics, text, textX, textY, GuiTheme.ACCENT);
    }

    private static void drawInfoStack(class_332 graphics, class_310 client) {
        class_2338 pos = client.field_1724.method_24515();
        double horizontalSpeed = Math.hypot(client.field_1724.method_18798().field_1352, client.field_1724.method_18798().field_1350) * 20.0D;
        HudSettings settings = DonutUtilitiesClient.MODULES.hudSettings();

        int x = 8;
        int y = 28;
        if (settings.coordinates()) y = drawHudPill(graphics, x, y, "XYZ " + pos.method_10263() + "/" + pos.method_10264() + "/" + pos.method_10260(), HudTheme.TEXT);
        if (settings.realTime()) y = drawHudPill(graphics, x, y, "TIME " + LocalTime.now().format(TIME_FORMAT), HudTheme.TEXT);
        if (settings.ping()) y = drawHudPill(graphics, x, y, "PING " + ping(client) + "ms", HudTheme.TEXT);
        if (settings.ticks()) y = drawHudPill(graphics, x, y, "TPS 20.0", HudTheme.TEXT);
        if (settings.bps()) drawHudPill(graphics, x, y, String.format(Locale.ROOT, "BPS %.2f", horizontalSpeed), HudTheme.TEXT);
    }

    private static int drawHudPill(class_332 graphics, int x, int y, String text, int color) {
        int width = HudTheme.scaledWidth(text) + 11;
        int height = 14;
        if (!NanoVgWatermarkRenderer.drawBackground(graphics, x, y, width, height, 0x8D151820)) {
            graphics.method_25294(x, y, x + width, y + height, 0x8D151820);
        }
        graphics.method_25294(x, y, x + 2, y + height, GuiTheme.ACCENT);
        int textY = y + Math.max(0, (height - Math.round(9 * HudTheme.TEXT_SCALE)) / 2);
        HudTheme.text(graphics, text, x + 6, textY, color);
        return y + 16;
    }

    private static int ping(class_310 client) {
        try {
            if (client.method_1562() == null || client.field_1724 == null) {
                return 0;
            }
            var info = client.method_1562().method_2871(client.field_1724.method_5667());
            return info == null ? 0 : info.method_2959();
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    private static void drawModuleList(class_332 graphics) {
        int screenWidth = class_310.method_1551().method_22683().method_4486();
        long now = System.currentTimeMillis();
        int y = 8;
        int edgeGap = 5;
        int right = screenWidth - edgeGap;
        int maxWidth = 250;
        int rowHeight = 12;
        int rowGap = 1;
        int textRightPadding = 2;
        int accentWidth = 2;
        int leftPadding = 3;
        int centerPadding = 0;

        syncModuleListStates(now);

        List<Module> visibleModules = DonutUtilitiesClient.MODULES.modules().stream()
                .filter(DonutHudRenderer::isModuleListVisible)
                .filter(module -> module.enabled() || stillExiting(module, now))
                .toList();

        for (Module module : visibleModules) {
            ModuleListState state = moduleListStates.get(module.id());
            float visible = moduleListProgress(state, now);
            if (visible <= 0.0F) {
                continue;
            }

            String name = module.name().toUpperCase(Locale.ROOT);
            String fitted = HudTheme.fit(name, HudTheme.unscaledWidth(maxWidth));
            int width = HudTheme.scaledWidth(fitted);
            int textAreaWidth = width + centerPadding;
            int rowWidth = textAreaWidth + leftPadding + textRightPadding + accentWidth;
            int slide = Math.round((rowWidth + 14) * (1.0F - visible));
            int animatedRight = right + slide;
            int rowX = animatedRight - rowWidth;
            int alpha = Math.round(255.0F * visible);

            graphics.method_25294(rowX, y, animatedRight, y + rowHeight, withAlpha(0x151820, Math.round(141.0F * visible)));
            graphics.method_25294(animatedRight - accentWidth, y, animatedRight, y + rowHeight, withAlpha(HudTheme.BLUE, alpha));
            int textAreaX = rowX + leftPadding;
            int textX = textAreaX + Math.max(0, (textAreaWidth - width) / 2);
            HudTheme.text(graphics, fitted, textX, y + 1, withAlpha(HudTheme.TEXT, alpha));
            y += rowHeight + rowGap;
        }
    }

    private static void syncModuleListStates(long now) {
        for (Module module : DonutUtilitiesClient.MODULES.modules()) {
            if (!isModuleListVisible(module)) {
                moduleListStates.remove(module.id());
                continue;
            }

            ModuleListState state = moduleListStates.get(module.id());
            if (state == null) {
                long startedAt = moduleListInitialized ? now : now - MODULE_LIST_ENTER_MS;
                moduleListStates.put(module.id(), new ModuleListState(module.enabled(), startedAt));
                continue;
            }
            if (state.enabled != module.enabled()) {
                state.enabled = module.enabled();
                state.changedAt = now;
            }
        }
        moduleListInitialized = true;
    }

    private static boolean isModuleListVisible(Module module) {
        return !module.id().equals("module_list")
                && !module.id().equals("click_gui")
                && !module.id().equals("hud")
                && !module.id().equals("popup");
    }

    private static boolean stillExiting(Module module, long now) {
        ModuleListState state = moduleListStates.get(module.id());
        return state != null && !state.enabled && now - state.changedAt < MODULE_LIST_EXIT_MS;
    }

    private static float moduleListProgress(ModuleListState state, long now) {
        if (state == null) {
            return 0.0F;
        }
        long elapsed = now - state.changedAt;
        float raw = state.enabled
                ? Math.min(1.0F, elapsed / (float) MODULE_LIST_ENTER_MS)
                : Math.max(0.0F, 1.0F - elapsed / (float) MODULE_LIST_EXIT_MS);
        return easeOut(raw);
    }

    private static float easeOut(float value) {
        return 1.0F - (1.0F - value) * (1.0F - value);
    }

    private static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }

    private static final class ModuleListState {
        private boolean enabled;
        private long changedAt;

        private ModuleListState(boolean enabled, long changedAt) {
            this.enabled = enabled;
            this.changedAt = changedAt;
        }
    }

    private static void drawSmallIcon(class_332 graphics, int x, int y, String icon) {
        int color = 0xFFD7DAE2;
        if (icon.equals("clock")) {
            RoundedRectRenderer.roundedRect(graphics, x, y, 11, 11, 5, 0x00000000);
            graphics.method_25294(x + 5, y + 2, x + 6, y + 6, color);
            graphics.method_25294(x + 5, y + 5, x + 9, y + 6, color);
            RoundedRectRenderer.roundedRect(graphics, x - 1, y - 1, 13, 13, 6, 0x34FFFFFF);
            return;
        }
        if (icon.equals("potion")) {
            RoundedRectRenderer.roundedRect(graphics, x + 3, y + 1, 5, 3, 2, color);
            RoundedRectRenderer.roundedRect(graphics, x + 1, y + 4, 9, 9, 4, 0x34FFFFFF);
            graphics.method_25294(x + 4, y + 7, x + 8, y + 10, color);
            return;
        }
        if (icon.equals("compass")) {
            RoundedRectRenderer.roundedRect(graphics, x - 1, y - 1, 13, 13, 6, 0x34FFFFFF);
            graphics.method_25294(x + 5, y + 2, x + 6, y + 10, color);
            graphics.method_25294(x + 2, y + 5, x + 10, y + 6, color);
            graphics.method_25294(x + 7, y + 3, x + 9, y + 5, HudTheme.BLUE);
            return;
        }
        if (icon.equals("scan")) {
            graphics.method_25294(x, y + 2, x + 4, y + 3, color);
            graphics.method_25294(x + 8, y + 2, x + 12, y + 3, color);
            graphics.method_25294(x, y + 9, x + 4, y + 10, color);
            graphics.method_25294(x + 8, y + 9, x + 12, y + 10, color);
            graphics.method_25294(x + 5, y + 5, x + 8, y + 8, HudTheme.BLUE);
            return;
        }
        if (icon.equals("radio")) {
            RoundedRectRenderer.roundedRect(graphics, x + 1, y + 1, 11, 11, 6, 0x34FFFFFF);
            graphics.method_25294(x + 4, y + 4, x + 8, y + 8, HudTheme.BLUE);
            graphics.method_25294(x + 2, y + 2, x + 4, y + 3, color);
            graphics.method_25294(x + 8, y + 2, x + 10, y + 3, color);
            graphics.method_25294(x + 2, y + 9, x + 4, y + 10, color);
            graphics.method_25294(x + 8, y + 9, x + 10, y + 10, color);
            return;
        }
        RoundedRectRenderer.roundedRect(graphics, x, y + 1, 12, 10, 2, 0x34FFFFFF);
        graphics.method_25294(x + 2, y + 4, x + 4, y + 6, color);
        graphics.method_25294(x + 5, y + 4, x + 7, y + 6, color);
        graphics.method_25294(x + 8, y + 4, x + 10, y + 6, color);
        graphics.method_25294(x + 3, y + 8, x + 9, y + 9, color);
    }
}
