package dev.prchl.donututilities.mixin;

import dev.prchl.donututilities.freecam.FreecamController;
import net.minecraft.class_310;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_312.class)
public abstract class MouseHandlerMixin {
    @Shadow
    private class_310 minecraft;

    @Shadow
    private double accumulatedDX;

    @Shadow
    private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void donututilities$turnFreecamOnly(double deltaTime, CallbackInfo info) {
        if (!FreecamController.active() || minecraft.field_1724 == null) {
            return;
        }

        double sensitivity = minecraft.field_1690.method_42495().method_41753() * 0.6000000238418579D + 0.20000000298023224D;
        double cubic = sensitivity * sensitivity * sensitivity;
        double multiplier = cubic * 8.0D;
        double dx = accumulatedDX * multiplier;
        double dy = accumulatedDY * multiplier;
        if (minecraft.field_1690.method_72706().method_41753()) {
            dx = -dx;
        }
        if (minecraft.field_1690.method_42438().method_41753()) {
            dy = -dy;
        }

        FreecamController.turn(dx, dy);
        info.cancel();
    }
}
