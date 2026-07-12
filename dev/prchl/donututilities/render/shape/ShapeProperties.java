package dev.prchl.donututilities.render.shape;

import net.minecraft.class_332;

public final class ShapeProperties {
    private final class_332 graphics;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final float radius;
    private final float softness;
    private final float thickness;
    private final int color;
    private final int outlineColor;

    private ShapeProperties(Builder builder) {
        this.graphics = builder.graphics;
        this.x = builder.x;
        this.y = builder.y;
        this.width = builder.width;
        this.height = builder.height;
        this.radius = builder.radius;
        this.softness = builder.softness;
        this.thickness = builder.thickness;
        this.color = builder.color;
        this.outlineColor = builder.outlineColor;
    }

    public static Builder create(class_332 graphics, int x, int y, int width, int height) {
        return new Builder(graphics, x, y, width, height);
    }

    public class_332 graphics() {
        return graphics;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public float radius() {
        return radius;
    }

    public float softness() {
        return softness;
    }

    public float thickness() {
        return thickness;
    }

    public int color() {
        return color;
    }

    public int outlineColor() {
        return outlineColor;
    }

    public static final class Builder {
        private final class_332 graphics;
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private float radius = 7.0F;
        private float softness = 0.0F;
        private float thickness = 0.0F;
        private int color = 0xFFFFFFFF;
        private int outlineColor = 0x00000000;

        private Builder(class_332 graphics, int x, int y, int width, int height) {
            this.graphics = graphics;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Builder round(float radius) {
            this.radius = radius;
            return this;
        }

        public Builder softness(float softness) {
            this.softness = softness;
            return this;
        }

        public Builder thickness(float thickness) {
            this.thickness = thickness;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder outlineColor(int outlineColor) {
            this.outlineColor = outlineColor;
            return this;
        }

        public ShapeProperties build() {
            return new ShapeProperties(this);
        }
    }
}
