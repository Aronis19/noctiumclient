package dev.prchl.donututilities.render;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.class_332;

final class NanoVgWatermarkRenderer {
    private static boolean attemptedInit;
    private static boolean unavailable;
    private static long context;
    private static Method beginFrame;
    private static Method endFrame;
    private static Method beginPath;
    private static Method roundedRect;
    private static Method fillColor;
    private static Method fill;
    private static Method colorCreate;
    private static Method colorR;
    private static Method colorG;
    private static Method colorB;
    private static Method colorA;
    private static Object color;

    private NanoVgWatermarkRenderer() {
    }

    static boolean drawBackground(class_332 graphics, int x, int y, int width, int height, int color) {
        if (!init()) {
            return false;
        }

        try {
            beginFrame.invoke(null, context, (float) graphics.method_51421(), (float) graphics.method_51443(), 1.0F);
            drawRound(x, y, width, height, 4.5F, color);
            endFrame.invoke(null, context);
            return true;
        } catch (Throwable exception) {
            unavailable = true;
            return false;
        }
    }

    private static boolean init() {
        if (unavailable) {
            return false;
        }
        if (attemptedInit) {
            return context != 0L;
        }
        attemptedInit = true;

        try {
            Class<?> nanoVg = Class.forName("org.lwjgl.nanovg.NanoVG");
            Class<?> nanoVgGl = Class.forName("org.lwjgl.nanovg.NanoVGGL3");
            Class<?> colorClass = Class.forName("org.lwjgl.nanovg.NVGColor");

            Method create = nanoVgGl.getMethod("nvgCreate", int.class);
            int flags = flag(nanoVgGl, "NVG_ANTIALIAS", 1);
            context = ((Number) create.invoke(null, flags)).longValue();
            if (context == 0L) {
                unavailable = true;
                return false;
            }

            beginFrame = nanoVg.getMethod("nvgBeginFrame", long.class, float.class, float.class, float.class);
            endFrame = nanoVg.getMethod("nvgEndFrame", long.class);
            beginPath = nanoVg.getMethod("nvgBeginPath", long.class);
            roundedRect = nanoVg.getMethod("nvgRoundedRect", long.class, float.class, float.class, float.class, float.class, float.class);
            fillColor = nanoVg.getMethod("nvgFillColor", long.class, colorClass);
            fill = nanoVg.getMethod("nvgFill", long.class);
            colorCreate = colorClass.getMethod("create");
            colorR = colorClass.getMethod("r", float.class);
            colorG = colorClass.getMethod("g", float.class);
            colorB = colorClass.getMethod("b", float.class);
            colorA = colorClass.getMethod("a", float.class);
            color = colorCreate.invoke(null);
            return true;
        } catch (Throwable exception) {
            unavailable = true;
            context = 0L;
            return false;
        }
    }

    private static void drawRound(float x, float y, float width, float height, float radius, int argb) throws ReflectiveOperationException {
        setColor(argb);
        beginPath.invoke(null, context);
        roundedRect.invoke(null, context, x, y, width, height, radius);
        fillColor.invoke(null, context, color);
        fill.invoke(null, context);
    }

    private static void setColor(int argb) throws ReflectiveOperationException {
        colorR.invoke(color, ((argb >> 16) & 0xFF) / 255.0F);
        colorG.invoke(color, ((argb >> 8) & 0xFF) / 255.0F);
        colorB.invoke(color, (argb & 0xFF) / 255.0F);
        colorA.invoke(color, ((argb >>> 24) & 0xFF) / 255.0F);
    }

    private static int flag(Class<?> owner, String name, int fallback) {
        try {
            Field field = owner.getField(name);
            return field.getInt(null);
        } catch (ReflectiveOperationException ignored) {
            return fallback;
        }
    }
}
