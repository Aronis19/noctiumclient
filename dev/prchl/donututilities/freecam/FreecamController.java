package dev.prchl.donututilities.freecam;

import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;

public final class FreecamController {
    private static class_243 position = class_243.field_1353;
    private static float yRot;
    private static float xRot;
    private static boolean active;
    private static long lastFrameNanos;

    private FreecamController() {
    }

    public static void enable(class_310 client) {
        if (client.field_1724 == null) {
            active = false;
            return;
        }

        position = new class_243(client.field_1724.method_23317(), client.field_1724.method_23320(), client.field_1724.method_23321());
        yRot = client.field_1724.method_36454();
        xRot = client.field_1724.method_36455();
        lastFrameNanos = System.nanoTime();
        active = true;
    }

    public static void disable() {
        active = false;
        lastFrameNanos = 0L;
    }

    public static boolean active() {
        return active;
    }

    public static class_243 position() {
        return position;
    }

    public static float yRot() {
        return yRot;
    }

    public static float xRot() {
        return xRot;
    }

    public static void tick(class_310 client, int speedSetting) {
        if (!active || client.field_1724 == null || client.field_1687 == null) {
            active = false;
            lastFrameNanos = 0L;
            return;
        }
    }

    public static void updateForRender(class_310 client, float renderYRot, float renderXRot, int speedSetting) {
        if (!active || client.field_1724 == null || client.field_1687 == null) {
            active = false;
            lastFrameNanos = 0L;
            return;
        }

        long now = System.nanoTime();
        if (lastFrameNanos == 0L) {
            lastFrameNanos = now;
            return;
        }

        double deltaSeconds = Math.min(0.05D, Math.max(0.0D, (now - lastFrameNanos) / 1_000_000_000.0D));
        lastFrameNanos = now;

        double forward = impulse(client.field_1690.field_1894.method_1434(), client.field_1690.field_1881.method_1434());
        double side = impulse(client.field_1690.field_1913.method_1434(), client.field_1690.field_1849.method_1434());
        double vertical = impulse(client.field_1690.field_1903.method_1434(), client.field_1690.field_1832.method_1434());
        if (forward == 0.0D && side == 0.0D && vertical == 0.0D) {
            return;
        }

        double speed = speedSetting * 2.4D * deltaSeconds;
        if (client.field_1690.field_1867.method_1434()) {
            speed *= 2.5D;
        }

        double yawRadians = Math.toRadians(yRot);
        class_243 forwardVector = new class_243(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
        class_243 sideVector = new class_243(forwardVector.field_1350, 0.0D, -forwardVector.field_1352);
        class_243 movement = forwardVector.method_1021(forward).method_1019(sideVector.method_1021(side)).method_1031(0.0D, vertical, 0.0D);
        if (movement.method_1027() > 1.0D) {
            movement = movement.method_1029();
        }

        position = position.method_1019(movement.method_1021(speed));
    }

    public static void turn(double xDelta, double yDelta) {
        if (!active) {
            return;
        }

        xRot = class_3532.method_15363(xRot + (float) yDelta * 0.15F, -90.0F, 90.0F);
        yRot += (float) xDelta * 0.15F;
    }

    private static double impulse(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0D;
        }
        return positive ? 1.0D : -1.0D;
    }
}
