package dev.prchl.donututilities.render;

import dev.prchl.donututilities.DonutUtilitiesClient;
import dev.prchl.donututilities.render.shape.RoundedRectRenderer;
import dev.prchl.donututilities.render.shape.ShapeProperties;
import net.minecraft.class_11719;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class GuiTheme {
    public static final int PANEL = 0xB0000000;
    public static final int PANEL_ALT = 0xC0000000;
    public static final int HEADER = 0xD0000000;
    public static final int ROW = 0x32000000;
    public static final int ROW_HOVER = 0x62000000;
    public static final int BORDER = 0x403E4655;
    public static final int SHADOW = 0x55000000;
    public static final int LINE = 0x453B4352;
    public static final int TOP_SHINE = 0x1AFFFFFF;
    public static final int HEADER_TEXT = 0xFFFFFFFF;
    public static final int TEXT = 0xFFD3D3D3;
    public static final int MUTED = 0xFFA7A7A7;
    public static final int ACCENT = 0xFF4677FF;
    public static final int ENABLED = 0xFF4DAC68;
    public static final int DISABLED = 0xFFA7A7A7;

    public static final int RADIUS = 5;
    public static final int HEADER_HEIGHT = 32;
    public static final int ROW_HEIGHT = 26;
    public static final float TEXT_SCALE = 1.0F;
    public static final float HEADER_TEXT_SCALE = 1.0F;

    private static final class_11719 UI_FONT = new class_11719.class_11721(
            class_2960.method_60655(DonutUtilitiesClient.MOD_ID, "minecraft_ten"));

    private GuiTheme() {
    }

    public static void panel(class_332 graphics, int x, int y, int width, int height) {
        rounded(graphics, x + 2, y + 4, width, height, RADIUS + 1, SHADOW);
        RoundedRectRenderer.render(ShapeProperties.create(graphics, x, y, width, height)
                .round(RADIUS)
                .thickness(1.0F)
                .outlineColor(BORDER)
                .color(PANEL)
                .build());
        graphics.method_25294(x + RADIUS, y + 1, x + width - RADIUS, y + 2, TOP_SHINE);
    }

    public static void header(class_332 graphics, int x, int y, int width, int height, String title, boolean selected) {
        RoundedRectRenderer.roundedTopRect(graphics, x + 1, y + 1, width - 2, height - 1, RADIUS - 1, HEADER);
        graphics.method_25294(x + 7, y + height, x + width - 7, y + height + 1, LINE);
        drawCategoryIcon(graphics, x + 10, y + 10, title, selected ? ACCENT : TEXT);
        text(graphics, fit(title, unscaledWidth(width - 58, HEADER_TEXT_SCALE), HEADER_TEXT_SCALE),
                x + 32, y + 9, selected ? ACCENT : HEADER_TEXT, HEADER_TEXT_SCALE);
        graphics.method_25294(x + width - 19, y + 14, x + width - 13, y + 16, selected ? ACCENT : MUTED);
    }

    public static void row(class_332 graphics, int x, int y, int width, int height, boolean hovered) {
        row(graphics, x, y, width, height, hovered ? 1.0F : 0.0F);
    }

    public static void row(class_332 graphics, int x, int y, int width, int height, float hoverAmount) {
        float amount = Math.max(0.0F, Math.min(1.0F, hoverAmount));
        rounded(graphics, x + 1, y, width - 2, height, 3, ROW);
        if (amount <= 0.01F) {
            return;
        }

        int hoverAlpha = Math.round(42.0F + 118.0F * amount);
        rounded(graphics, x + 1, y, width - 2, height, 3, (hoverAlpha << 24) | 0x202838);
        int shineAlpha = Math.round(20.0F + 60.0F * amount);
        graphics.method_25294(x + 8, y + 1, x + width - 8, y + 2, (shineAlpha << 24) | 0xFFFFFF);
        int accentAlpha = Math.round(38.0F + 82.0F * amount);
        RoundedRectRenderer.render(ShapeProperties.create(graphics, x + 1, y, width - 2, height)
                .round(3.0F)
                .thickness(1.0F)
                .outlineColor((accentAlpha << 24) | 0x4677FF)
                .color(0x00000000)
                .build());
    }

    public static void selection(class_332 graphics, int x, int y, int width, int height, boolean hovered) {
        rounded(graphics, x, y, width, height, 7, hovered ? 0x804677FF : 0x603B5FAE);
        RoundedRectRenderer.render(ShapeProperties.create(graphics, x, y, width, height)
                .round(7.0F)
                .thickness(1.0F)
                .outlineColor(0xAA4677FF)
                .color(0x00000000)
                .build());
    }

    public static void toggle(class_332 graphics, int x, int y, boolean enabled) {
        int track = enabled ? 0xCC4677FF : 0x66333333;
        int knob = 0xFFFFFFFF;
        rounded(graphics, x, y, 26, 13, 7, track);
        rounded(graphics, enabled ? x + 15 : x + 2, y + 2, 9, 9, 5, knob);
    }

    public static void searchBox(class_332 graphics, int x, int y, int width, int height, String text) {
        RoundedRectRenderer.render(ShapeProperties.create(graphics, x, y, width, height)
                .round(7.0F)
                .thickness(1.0F)
                .outlineColor(0x553E4655)
                .color(0xAA000000)
                .build());
        mutedText(graphics, fit(text, unscaledWidth(width - 18)), x + 8, y + 6);
    }

    public static void text(class_332 graphics, String text, int x, int y, int color) {
        text(graphics, text, x, y, color, TEXT_SCALE);
    }

    public static void mutedText(class_332 graphics, String text, int x, int y) {
        text(graphics, text, x, y, MUTED);
    }

    public static void accentText(class_332 graphics, String text, int x, int y) {
        text(graphics, text, x, y, ACCENT);
    }

    public static void inset(class_332 graphics, int x, int y, int width, int height) {
        RoundedRectRenderer.render(ShapeProperties.create(graphics, x, y, width, height)
                .round(7.0F)
                .thickness(1.0F)
                .outlineColor(0x47363C48)
                .color(0x7A11151C)
                .build());
    }

    public static String fit(String text, int maxWidth) {
        return fit(text, maxWidth, TEXT_SCALE);
    }

    public static int scaledWidth(String text) {
        return scaledWidth(text, TEXT_SCALE);
    }

    public static int unscaledWidth(int scaledWidth) {
        return unscaledWidth(scaledWidth, TEXT_SCALE);
    }

    private static void text(class_332 graphics, String text, int x, int y, int color, float scale) {
        class_2561 component = class_2561.method_43470(text).method_27694(style -> style.method_27704(UI_FONT));
        graphics.method_51448().pushMatrix();
        graphics.method_51448().scale(scale, scale);
        int sx = Math.round(x / scale);
        int sy = Math.round(y / scale);
        graphics.method_51439(class_310.method_1551().field_1772, component, sx + 1, sy + 1, 0x52000000, false);
        graphics.method_51439(class_310.method_1551().field_1772, component, sx, sy, color, false);
        graphics.method_51448().popMatrix();
    }

    private static String fit(String text, int maxWidth, float scale) {
        String value = text;
        while (!value.isEmpty() && scaledWidth(value, scale) > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static int scaledWidth(String text, float scale) {
        return Math.round(class_310.method_1551().field_1772.method_1727(text) * scale);
    }

    private static int unscaledWidth(int scaledWidth, float scale) {
        return Math.max(1, Math.round(scaledWidth / scale));
    }

    private static void rounded(class_332 graphics, int x, int y, int width, int height, int radius, int color) {
        RoundedRectRenderer.roundedRect(graphics, x, y, width, height, radius, color);
    }

    private static void drawCategoryIcon(class_332 graphics, int x, int y, String title, int color) {
        class_1799 stack = switch (title) {
            case "MISC" -> new class_1799(class_1802.field_8857);
            case "BASEFINDING" -> new class_1799(class_1802.field_8545);
            case "DONUT" -> new class_1799(class_1802.field_17532);
            case "CLIENT" -> new class_1799(class_1802.field_8137);
            case "SEARCH" -> new class_1799(class_1802.field_27070);
            case "RENDER" -> new class_1799(class_1802.field_8449);
            default -> class_1799.field_8037;
        };
        if (!stack.method_7960()) {
            graphics.method_51427(stack, x - 2, y - 2);
        } else {
            rounded(graphics, x + 1, y + 1, 9, 9, 2, 0x33000000);
            graphics.method_25294(x + 3, y + 3, x + 8, y + 8, color);
        }
    }
}
