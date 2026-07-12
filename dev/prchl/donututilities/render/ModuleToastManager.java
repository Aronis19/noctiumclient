package dev.prchl.donututilities.render;

import dev.prchl.donututilities.DonutUtilitiesClient;
import dev.prchl.donututilities.module.Module;
import dev.prchl.donututilities.module.ModuleCategory;
import dev.prchl.donututilities.module.ModuleManager;
import dev.prchl.donututilities.module.PopupSettings;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_332;

public final class ModuleToastManager {
    private static final long DISPLAY_TIME_MS = 3_200L;
    private static final long ENTER_TIME_MS = 260L;
    private static final long EXIT_TIME_MS = 300L;
    private static final int MIN_WIDTH = 120;
    private static final int MAX_WIDTH = 165;
    private static final int HEIGHT = 32;
    private static final Map<String, Boolean> knownStates = new HashMap<>();

    private static String messageTitle;
    private static String messageStatus;
    private static boolean messageEnabled;
    private static long messageStartedAt;
    private static class_1799 messageIcon = class_1799.field_8037;

    private ModuleToastManager() {
    }

    public static void tick(ModuleManager modules) {
        for (Module module : modules.modules()) {
            Boolean previous = knownStates.put(module.id(), module.enabled());
            if (previous != null && previous != module.enabled()) {
                show(module, module.enabled());
            }
        }
    }

    public static void show(Module module, boolean enabled) {
        messageTitle = module.name().toUpperCase(java.util.Locale.ROOT);
        messageStatus = enabled ? "HAS BEEN ENABLED." : "HAS BEEN DISABLED.";
        messageEnabled = enabled;
        messageIcon = iconFor(module.category());
        messageStartedAt = System.currentTimeMillis();
    }

    public static void render(class_332 graphics) {
        if (messageTitle == null) {
            return;
        }
        if (!DonutUtilitiesClient.MODULES.enabled("popup")) {
            return;
        }

        long elapsed = System.currentTimeMillis() - messageStartedAt;
        if (elapsed >= DISPLAY_TIME_MS) {
            messageTitle = null;
            messageStatus = null;
            return;
        }

        float enter = Math.min(1.0F, elapsed / (float) ENTER_TIME_MS);
        float exit = elapsed > DISPLAY_TIME_MS - EXIT_TIME_MS
                ? Math.max(0.0F, (DISPLAY_TIME_MS - elapsed) / (float) EXIT_TIME_MS)
                : 1.0F;
        float visible = easeOut(Math.min(enter, exit));
        int screenWidth = graphics.method_51421();
        int screenHeight = graphics.method_51443();
        int width = toastWidth();
        PopupSettings.Position position = DonutUtilitiesClient.MODULES.popupSettings().position();
        int targetX = targetX(position, screenWidth, width);
        int targetY = targetY(position, screenHeight);
        int x = animatedX(position, targetX, screenWidth, width, visible);
        int y = animatedY(position, targetY, screenHeight, visible);
        int alpha = Math.round(255.0F * exit);

        int panel = withAlpha(0x161A23, Math.round(232.0F * exit));
        int text = withAlpha(0xF0F2F6, alpha);
        int accent = withAlpha(GuiTheme.ACCENT & 0x00FFFFFF, alpha);

        HudTheme.frame(graphics, x, y, width, HEIGHT);
        HudTheme.rect(graphics, x + 2, y + 2, width - 4, HEIGHT - 4, panel);
        if (!messageIcon.method_7960()) {
            graphics.method_51427(messageIcon, x + 5, y + 7);
        }
        int textWidth = GuiTheme.unscaledWidth(width - 36);
        GuiTheme.text(graphics, GuiTheme.fit(messageTitle, textWidth), x + 27, y + 6, text);
        GuiTheme.text(graphics, GuiTheme.fit(messageStatus, textWidth), x + 27, y + 17, text);

        int barX = x + 14;
        int barY = y + HEIGHT - 5;
        int barWidth = width - 28;
        graphics.method_25294(barX, barY, barX + barWidth, barY + 1, withAlpha(0x343A46, Math.round(180.0F * exit)));
        graphics.method_25294(barX, barY, barX + Math.round(barWidth * progress(elapsed)), barY + 1, accent);
    }

    private static class_1799 iconFor(ModuleCategory category) {
        return switch (category) {
            case MISC -> new class_1799(class_1802.field_8857);
            case BASE -> new class_1799(class_1802.field_8545);
            case DONUT -> new class_1799(class_1802.field_17532);
            case RENDER -> new class_1799(class_1802.field_8449);
            case CLIENT -> new class_1799(class_1802.field_8137);
        };
    }

    private static int toastWidth() {
        int contentWidth = Math.max(GuiTheme.scaledWidth(messageTitle), GuiTheme.scaledWidth(messageStatus));
        return Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, contentWidth + 42));
    }

    private static int targetX(PopupSettings.Position position, int screenWidth, int width) {
        return switch (position) {
            case TOP_LEFT, BOTTOM_LEFT -> 12;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - width - 12;
            case TOP_CENTER, BOTTOM_CENTER -> (screenWidth - width) / 2;
        };
    }

    private static int targetY(PopupSettings.Position position, int screenHeight) {
        return switch (position) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 12;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> screenHeight - HEIGHT - 12;
        };
    }

    private static int animatedX(PopupSettings.Position position, int target, int screenWidth, int width, float progress) {
        return Math.round(screenWidth + 8 - (screenWidth - target + 8) * progress);
    }

    private static int animatedY(PopupSettings.Position position, int target, int screenHeight, float progress) {
        return target;
    }

    private static float progress(long elapsed) {
        return Math.max(0.0F, Math.min(1.0F, 1.0F - elapsed / (float) DISPLAY_TIME_MS));
    }

    private static float easeOut(float value) {
        return 1.0F - (1.0F - value) * (1.0F - value);
    }

    private static int withAlpha(int rgb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0x00FFFFFF);
    }
}
