package dev.prchl.donututilities.render;

import dev.prchl.donututilities.DonutUtilitiesClient;
import dev.prchl.donututilities.render.shape.RoundedRectRenderer;
import dev.prchl.donututilities.render.shape.ShapeProperties;
import net.minecraft.class_11719;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class HudTheme {
    public static final int PANEL = 0xD71E2029;
    public static final int PANEL_ALT = 0xE3292B35;
    public static final int HEADER = 0xE4171921;
    public static final int BORDER = 0x68454B59;
    public static final int INNER_BORDER = 0x283A4050;
    public static final int SHADOW = 0x5C000000;
    public static final int TOP_SHINE = 0x20FFFFFF;
    public static final int LINE = 0x5A373D49;

    public static final int BLUE = 0xFF79C8FF;
    public static final int TEXT = 0xFFE2E4EA;
    public static final int MUTED = 0xFFAEB3BD;
    public static final int ACCENT = 0xFFF1EA45;

    public static final int RADIUS = 9;
    public static final int HEADER_HEIGHT = 25;
    public static final float TEXT_SCALE = 0.86F;

    private static final class_11719 UI_FONT =
            new class_11719.class_11721(class_2960.method_60655(DonutUtilitiesClient.MOD_ID, "minecraft_ten"));

    private HudTheme() {
    }

    public static void frame(class_332 graphics, int x, int y, int width, int height) {
        softShadow(graphics, x, y, width, height);
        glassRect(graphics, x, y, width, height);
    }

    public static void header(class_332 graphics, int x, int y, int width, String title) {
        header(graphics, x, y, width, title, TEXT);
    }

    public static void header(class_332 graphics, int x, int y, int width, String title, int color) {
        RoundedRectRenderer.roundedTopRect(graphics, x + 1, y + 1, width - 2, HEADER_HEIGHT - 1, RADIUS - 1, HEADER);
        graphics.method_25294(x + 7, y + HEADER_HEIGHT, x + width - 7, y + HEADER_HEIGHT + 1, LINE);
        text(graphics, fit(title, unscaledWidth(width - 28)), x + 11, y + 7, color);
    }

    public static void text(class_332 graphics, String text, int x, int y, int color) {
        class_2561 component = class_2561.method_43470(text).method_27694(style -> style.method_27704(UI_FONT));
        graphics.method_51448().pushMatrix();
        graphics.method_51448().scale(TEXT_SCALE, TEXT_SCALE);
        int sx = Math.round(x / TEXT_SCALE);
        int sy = Math.round(y / TEXT_SCALE);
        graphics.method_51439(class_310.method_1551().field_1772, component, sx + 1, sy + 1, 0x6605070A, false);
        graphics.method_51439(class_310.method_1551().field_1772, component, sx, sy, color, false);
        graphics.method_51448().popMatrix();
    }

    public static void rect(class_332 graphics, int x, int y, int width, int height, int color) {
        RoundedRectRenderer.render(ShapeProperties.create(graphics, x, y, width, height)
                .round(RADIUS)
                .color(color)
                .build());
    }

    public static void glassRect(class_332 graphics, int x, int y, int width, int height) {
        RoundedRectRenderer.render(ShapeProperties.create(graphics, x, y, width, height)
                .round(RADIUS)
                .softness(1.0F)
                .thickness(1.5F)
                .outlineColor(BORDER)
                .color(PANEL)
                .build());
        RoundedRectRenderer.render(ShapeProperties.create(graphics, x + 1, y + 1, width - 2, height - 2)
                .round(RADIUS - 1)
                .thickness(1.0F)
                .outlineColor(INNER_BORDER)
                .color(0x00000000)
                .build());
        graphics.method_25294(x + RADIUS, y + 1, x + width - RADIUS, y + 2, TOP_SHINE);
    }

    public static void row(class_332 graphics, int x, int y, int width, int height, boolean hovered) {
        RoundedRectRenderer.render(ShapeProperties.create(graphics, x, y, width, height)
                .round(5.0F)
                .thickness(1.0F)
                .outlineColor(hovered ? 0x384A5262 : 0x163A4050)
                .color(hovered ? 0xA82A2D38 : 0x34191B23)
                .build());
    }

    public static void inset(class_332 graphics, int x, int y, int width, int height) {
        RoundedRectRenderer.render(ShapeProperties.create(graphics, x, y, width, height)
                .round(7.0F)
                .thickness(1.0F)
                .outlineColor(0x35343A48)
                .color(0x850B0D12)
                .build());
    }

    public static void blurredRect(class_332 graphics, int x, int y, int width, int height) {
        glassRect(graphics, x, y, width, height);
    }

    public static String fit(String text, int maxWidth) {
        String value = text;
        while (!value.isEmpty() && scaledWidth(value) > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    public static int scaledWidth(String text) {
        return Math.round(class_310.method_1551().field_1772.method_1727(text) * TEXT_SCALE);
    }

    public static int unscaledWidth(int scaledWidth) {
        return Math.max(1, Math.round(scaledWidth / TEXT_SCALE));
    }

    private static void softShadow(class_332 graphics, int x, int y, int width, int height) {
        RoundedRectRenderer.render(ShapeProperties.create(graphics, x + 3, y + 5, width, height)
                .round(RADIUS + 1)
                .color(0x3D000000)
                .build());
        RoundedRectRenderer.render(ShapeProperties.create(graphics, x + 2, y + 3, width, height)
                .round(RADIUS)
                .color(SHADOW)
                .build());
    }
}
