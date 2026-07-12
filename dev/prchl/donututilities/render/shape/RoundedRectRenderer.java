package dev.prchl.donututilities.render.shape;

import net.minecraft.class_332;

public final class RoundedRectRenderer {
    private RoundedRectRenderer() {
    }

    public static void render(ShapeProperties properties) {
        if (properties.width() <= 0 || properties.height() <= 0) {
            return;
        }

        int thickness = Math.max(0, Math.round(properties.thickness()));
        if (thickness > 0 && alpha(properties.outlineColor()) > 0) {
            roundedRect(properties.graphics(), properties.x(), properties.y(), properties.width(), properties.height(),
                    Math.round(properties.radius()), properties.outlineColor());
        }

        int inset = thickness > 0 ? thickness : 0;
        if (alpha(properties.color()) > 0) {
            roundedRect(properties.graphics(), properties.x() + inset, properties.y() + inset,
                    properties.width() - inset * 2, properties.height() - inset * 2,
                    Math.max(0, Math.round(properties.radius()) - inset), properties.color());
        }
    }

    public static void roundedRect(class_332 graphics, int x, int y, int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0 || alpha(color) <= 0) {
            return;
        }

        int r = Math.min(Math.max(0, radius), Math.min(width, height) / 2);
        if (r <= 0) {
            graphics.method_25294(x, y, x + width, y + height, color);
            return;
        }

        graphics.method_25294(x + r, y, x + width - r, y + height, color);
        graphics.method_25294(x, y + r, x + width, y + height - r, color);
        for (int row = 0; row < r; row++) {
            int inset = roundedInset(r, row);
            graphics.method_25294(x + inset, y + row, x + width - inset, y + row + 1, color);
            graphics.method_25294(x + inset, y + height - row - 1, x + width - inset, y + height - row, color);
        }
    }

    public static void roundedTopRect(class_332 graphics, int x, int y, int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0 || alpha(color) <= 0) {
            return;
        }

        int r = Math.min(Math.max(0, radius), Math.min(width, height + radius) / 2);
        graphics.method_25294(x + r, y, x + width - r, y + height, color);
        graphics.method_25294(x, y + r, x + width, y + height, color);
        for (int row = 0; row < r; row++) {
            int inset = roundedInset(r, row);
            graphics.method_25294(x + inset, y + row, x + width - inset, y + row + 1, color);
        }
    }

    private static int roundedInset(int radius, int row) {
        double yOffset = radius - row - 0.5D;
        double xOffset = radius - Math.sqrt(Math.max(0.0D, radius * radius - yOffset * yOffset));
        return Math.max(0, (int) Math.ceil(xOffset));
    }

    private static int alpha(int color) {
        return color >>> 24;
    }
}
